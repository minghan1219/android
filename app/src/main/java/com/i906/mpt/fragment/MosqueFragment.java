package com.i906.mpt.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.i906.mpt.adapter.MosqueAdapter;
import com.i906.mpt.model.Mosque;
import com.i906.mpt.view.DividerItemDecoration;

import java.util.List;
import java.util.Locale;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MosqueFragment extends BaseRecyclerFragment implements MosqueAdapter.MosqueListener {

    private CompositeSubscription mSubscription = new CompositeSubscription();
    private LinearLayoutManager mLayoutManager;
    private MosqueAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MosqueAdapter();
        mAdapter.addMosqueListener(this);
        mAdapter.setMosqueList(mMosqueHelper.getCachedMosques());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAdapter.isEmpty()) {
            onRefresh();
        } else {
            setListShown(true, false);
        }
    }

    @Override
    public void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        if (mRecyclerView.getAdapter() == null) mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        Subscription s = mMosqueHelper.getNearbyMosques()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Mosque>>() {
                    @Override
                    public void onCompleted() {
                        mListContainer.setRefreshing(false);
                        setListShown(true, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Mosque> mosques) {
                        mAdapter.setMosqueList(mosques);
                    }
                });

        mSubscription.add(s);
    }

    @Override
    public void onMosqueSelected(Mosque mosque) {
        String name = mosque.getName();
        double lat = mosque.getLatitude();
        double lng = mosque.getLongitude();

        String coordinates = String.format(Locale.ENGLISH, "%f,%f", lat, lng);
        String uri = String.format(Locale.ENGLISH, "geo:%s(%s)?q=%s (%s)", coordinates, name,
                coordinates, name);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSubscription.unsubscribe();
        mAdapter.removeMosqueListener(this);
    }
}

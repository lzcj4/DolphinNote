package com.tannuo.note.server;

import com.tannuo.sdk.util.Logger;

import rx.Subscriber;

/**
 * Created by Nick_PC on 2016/7/6.
 */
public class DefaultSubscribe<T> extends Subscriber<T> {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCompleted() {

        Logger.d(TAG, "onCompleted  invoked");
    }

    @Override
    public void onError(Throwable e) {

        Logger.e(TAG, String.format("onError invoked:%s", e.getMessage()));
    }

    @Override
    public void onNext(T t) {

        Logger.d(TAG, String.format("onNext invoked:%s", t != null ? t.toString() : " t is null"));
    }
}

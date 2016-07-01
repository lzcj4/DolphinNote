package com.tannuo.sdk.device;

/**
 * Created by nick on 2016/04/22.
 */
public interface TouchPointListener {

    /**
     * on down / move / up event / snapshot
     * @param touchEvent
     */
    void onTouchEvent(TouchEvent touchEvent);

    /**
     * point error
     *
     * @param errorCode
     */
    void onError(int errorCode);
}




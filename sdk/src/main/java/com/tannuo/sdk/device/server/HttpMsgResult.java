package com.tannuo.sdk.device.server;

/**
 * Created by Nick_PC on 2016/7/6.
 */
public class HttpMsgResult<T> extends HttpResult<T> {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

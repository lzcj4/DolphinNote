package com.tannuo.note.server;

/**
 * Created by Nick_PC on 2016/7/24.
 */
public class HttpMeetingResult<T>  extends  HttpResult<T> {
    private String meetingUrl;

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String message) {
        this.meetingUrl = message;
    }
}

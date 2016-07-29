package com.tannuo.sdk.device.server;

import java.util.List;

/**
 * Created by Nick_PC on 2016/7/27.
 */
public class HttpCodeResult<T> {
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static class HttpWXLoginResult<T> extends HttpCodeResult {
        private T user;

        public T getData() {
            return user;
        }

    }

    public static class HttpAliveConfResult<T> extends HttpCodeResult {
        private List<T> conferences;

        public List<T> getData() {
            return conferences;
        }

    }

    public static class HttpLaunchConfResult<T> extends HttpCodeResult {
        private T conference;
        private String meetingUrl;

        public T getData() {
            return conference;
        }


        public String getMeetingUrl() {
            return meetingUrl;
        }

        public void setMeetingUrl(String data) {
            this.meetingUrl = data;
        }
    }
}

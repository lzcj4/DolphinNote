package com.tannuo.sdk.device.server;

/**
 * Created by Nick_PC on 2016/5/12.
 */
public class ServerConfig {
    /**
     * pollingFrameInterval : 1000
     * postFrameInterval : 1000
     * maxContinuousFailureCount : 10
     * postSnapshotInterval : 30000
     * postHeartbeatInterval : 20000
     * clientConnectTimeout : 10000
     * clientReadTimeout : 10000
     * ruberMaxSize : 14040316
     * ruberMinSize : 3509376
     * penMaxSize : 493719
     * penMinSize : 8777
     * penWidth : 4.5
     * whConverse : false
     * renderSmooth : false
     * wxAppKey : wx1681c34b79f4013a
     * wxAppSecret : 7aee473ffd21d083f0d894c7b8028ae4
     * wxAppState : thisisastate
     */

    private int pollingFrameInterval;
    private int postFrameInterval;
    private int maxContinuousFailureCount;
    private int postSnapshotInterval;
    private int postHeartbeatInterval;
    private int clientConnectTimeout;
    private int clientReadTimeout;
    private int ruberMaxSize;
    private int ruberMinSize;
    private int penMaxSize;
    private int penMinSize;
    private double penWidth;
    private boolean whConverse;
    private boolean renderSmooth;
    private String wxAppKey;
    private String wxAppSecret;
    private String wxAppState;

    public int getPollingFrameInterval() {
        return pollingFrameInterval;
    }

    public void setPollingFrameInterval(int pollingFrameInterval) {
        this.pollingFrameInterval = pollingFrameInterval;
    }

    public int getPostFrameInterval() {
        return postFrameInterval;
    }

    public void setPostFrameInterval(int postFrameInterval) {
        this.postFrameInterval = postFrameInterval;
    }

    public int getMaxContinuousFailureCount() {
        return maxContinuousFailureCount;
    }

    public void setMaxContinuousFailureCount(int maxContinuousFailureCount) {
        this.maxContinuousFailureCount = maxContinuousFailureCount;
    }

    public int getPostSnapshotInterval() {
        return postSnapshotInterval;
    }

    public void setPostSnapshotInterval(int postSnapshotInterval) {
        this.postSnapshotInterval = postSnapshotInterval;
    }

    public int getPostHeartbeatInterval() {
        return postHeartbeatInterval;
    }

    public void setPostHeartbeatInterval(int postHeartbeatInterval) {
        this.postHeartbeatInterval = postHeartbeatInterval;
    }

    public int getClientConnectTimeout() {
        return clientConnectTimeout;
    }

    public void setClientConnectTimeout(int clientConnectTimeout) {
        this.clientConnectTimeout = clientConnectTimeout;
    }

    public int getClientReadTimeout() {
        return clientReadTimeout;
    }

    public void setClientReadTimeout(int clientReadTimeout) {
        this.clientReadTimeout = clientReadTimeout;
    }

    public int getRuberMaxSize() {
        return ruberMaxSize;
    }

    public void setRuberMaxSize(int ruberMaxSize) {
        this.ruberMaxSize = ruberMaxSize;
    }

    public int getRuberMinSize() {
        return ruberMinSize;
    }

    public void setRuberMinSize(int ruberMinSize) {
        this.ruberMinSize = ruberMinSize;
    }

    public int getPenMaxSize() {
        return penMaxSize;
    }

    public void setPenMaxSize(int penMaxSize) {
        this.penMaxSize = penMaxSize;
    }

    public int getPenMinSize() {
        return penMinSize;
    }

    public void setPenMinSize(int penMinSize) {
        this.penMinSize = penMinSize;
    }

    public double getPenWidth() {
        return penWidth;
    }

    public void setPenWidth(double penWidth) {
        this.penWidth = penWidth;
    }

    public boolean isWhConverse() {
        return whConverse;
    }

    public void setWhConverse(boolean whConverse) {
        this.whConverse = whConverse;
    }

    public boolean isRenderSmooth() {
        return renderSmooth;
    }

    public void setRenderSmooth(boolean renderSmooth) {
        this.renderSmooth = renderSmooth;
    }

    public String getWxAppKey() {
        return wxAppKey;
    }

    public void setWxAppKey(String wxAppKey) {
        this.wxAppKey = wxAppKey;
    }

    public String getWxAppSecret() {
        return wxAppSecret;
    }

    public void setWxAppSecret(String wxAppSecret) {
        this.wxAppSecret = wxAppSecret;
    }

    public String getWxAppState() {
        return wxAppState;
    }

    public void setWxAppState(String wxAppState) {
        this.wxAppState = wxAppState;
    }
}

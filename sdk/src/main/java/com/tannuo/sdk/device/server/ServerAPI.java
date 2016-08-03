package com.tannuo.sdk.device.server;

import com.tannuo.sdk.device.TouchFrame;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Nick_PC on 2016/5/12.
 */
public class ServerAPI {

    private OkHttpClient client;

    IServerAPI serverAPI;
    //final String URI_BASE = "http://tn.glasslink.cn:3000/";
    //final String URI_BASE = "http://10.10.10.205:3000/";
    final String URI_BASE = "http://192.168.1.120:3000/";
    private static final String MIME_STREAM = "application/octet-stream";

    {
        final int TIMEOUT = 60;
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URI_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        serverAPI = retrofit.create(IServerAPI.class);
    }

    private static class SingletonHolder {
        private static final ServerAPI INSTANCE = new ServerAPI();
    }

    public static ServerAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static class HttpResultFunc<T> implements Func1<HttpResult<T>, T> {
        @Override
        public T call(HttpResult<T> httpResult) {
            if (httpResult.getCode() != 0) {
                throw new IllegalArgumentException();
            }
            return httpResult.getData();
        }
    }

    public void getServerConfig() {

        toSubscribe(serverAPI.getConfigRx(), new DefaultSubscribe<>());

//        serverAPI.getConfig().enqueue(new Callback<ServerConfig>() {
//            @Override
//            public void onResponse(Call<ServerConfig> call, Response<ServerConfig> response) {
//
//            }
//
//            @Override
//            public void onFailure(Call<ServerConfig> call, Throwable t) {
//
//            }
//        });
    }

    public void getAliveConfs(Subscriber<List<Conference>> subscriber) {
        toSubscribe(serverAPI.getAliveConfsRx().map(new HttpResultFunc<>()), subscriber);
    }

    public void wxLogin(User user, Subscriber<User> subscriber) {
        toSubscribe(serverAPI.wxLoginRx(user).map(new HttpResultFunc<>()), subscriber);
    }

    public void createConf(Conference conf, Subscriber<HttpMeetingResult<Conference>> subscriber) {
        toSubscribe(serverAPI.createConfRx(conf), subscriber);
    }

    public void joinConf(Conference conf, Subscriber<HttpMeetingResult<Conference>> subscriber) {
        toSubscribe(serverAPI.joinConfRx(conf), subscriber);
    }

    public void endConf(Conference conf, Subscriber<HttpMeetingResult<Conference>> subscriber) {
        toSubscribe(serverAPI.endConfRx(conf), subscriber);
    }

    public void updateConf(Conference conf, Subscriber<HttpMeetingResult<Conference>> subscriber) {
        toSubscribe(serverAPI.updateConfRx(conf), subscriber);
    }

    public void postConfData(String baseUrl, String meetingId, TouchFrame frame,
                             Subscriber<Response<ResponseBody>> subscriber) {

        String url = baseUrl + String.format("/meeting/%s/frame", meetingId);
        MediaType mediaType = MediaType.parse(MIME_STREAM);
        byte[] data = frame.getBytes();
        RequestBody body = RequestBody.create(mediaType, data);
        toSubscribe(serverAPI.postConfDataRx(url, body), subscriber);
    }

    public void getConfData(String baseUrl, String meetingId, int seqId,
                            Subscriber<Response<ResponseBody>> subscriber) {

        String url = baseUrl + String.format("/meeting/%s/frames/%d", meetingId, seqId);
        toSubscribe(serverAPI.getConfDataRx(url), subscriber);
    }

    public void postImage(String baseUrl, String meetingId, String filePath,
                          Subscriber<Response<ResponseBody>> subscriber) {

        String url = baseUrl + String.format("/meeting/%s/snapshot", meetingId);
        File file = new File(filePath);
        if (file.exists()) {
            RequestBody body = RequestBody.create(MediaType.parse(MIME_STREAM), file);
            toSubscribe(serverAPI.postImageRx(url, body), subscriber);
        }
    }

    public void postHeartbeat(String meetingId,
                              Subscriber<HttpResult<Void>> subscriber) {
        toSubscribe(serverAPI.postHeartbeat(meetingId), subscriber);
    }

    public void getUserOnLine(String meetingId, String userId,
                              Subscriber<HttpResult<Void>> subscriber) {
        toSubscribe(serverAPI.getUserOnlineRx(meetingId, userId), subscriber);
    }

    public void getUserOffLine(String meetingId, String userId,
                               Subscriber<HttpResult<Void>> subscriber) {
        toSubscribe(serverAPI.getUserOfflineRx(meetingId, userId), subscriber);
    }

    public void toSubscribe(Observable o, Subscriber s) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }


}

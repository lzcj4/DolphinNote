package com.tannuo.note.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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

    OkHttpClient client;

    IServerAPI serverAPI;
    final String URI_BASE = "http://tn.glasslink.cn:3000/";

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

    private class HttpResultFunc<T> implements Func1<HttpResult<T>, T> {
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

    public void getAliveConfs() {
        toSubscribe(serverAPI.getAliveConfsRx().map(new HttpResultFunc<>()), new DefaultSubscribe<>());
    }

    public void createConf() {
        Conference conf = new Conference();
        Conference.ConferenceBean bean = new Conference.ConferenceBean();
        bean.setName("test");
        bean.setPassword("123");
        bean.setTechBridgeId("114893792");
        List<String> nickNames = new ArrayList<>();
        nickNames.add("Nick");
        bean.setNicknames(nickNames);
        bean.setUsers(nickNames);
        bean.setDatetime(1466471100000l);
        conf.setConference(bean);
        toSubscribe(serverAPI.createConfRx(conf).map(new HttpResultFunc<>()), new DefaultSubscribe<>());
    }

    private void toSubscribe(Observable o, Subscriber s) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s);
    }
}

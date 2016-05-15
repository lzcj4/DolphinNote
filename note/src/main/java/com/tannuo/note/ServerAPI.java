package com.tannuo.note;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Nick_PC on 2016/5/12.
 */
public class ServerAPI {

    OkHttpClient client;

    IServerAPI serverAPI;

    {
        final int TIMEOUT = 60;
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://tn.glasslink.cn:3000/").
                addConverterFactory(GsonConverterFactory.create()).client(client).build();

        serverAPI = retrofit.create(IServerAPI.class);
    }

    public void getServerConfig() {
//        Observable.create((subscriber -> {}))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe((obj)->{});
        serverAPI.getConfigByRxJava()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((r) -> {
                    Log.i("test",r.getWxAppKey());
                });

        serverAPI.getConfig().enqueue(new Callback<ServerConfig>() {
            @Override
            public void onResponse(Call<ServerConfig> call, Response<ServerConfig> response) {

            }

            @Override
            public void onFailure(Call<ServerConfig> call, Throwable t) {

            }
        });
    }
}

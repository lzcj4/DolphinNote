package com.tannuo.note;


import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by Nick_PC on 2016/5/12.
 */
public interface IServerAPI {

    @GET("/appconfig")
    Call<ServerConfig> getConfig();

    @GET("/appconfig")
    Observable<HttpResult<ServerConfig>> getConfigByRxJava();
}

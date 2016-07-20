package com.tannuo.note.server;


import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Nick on 2016/5/12.
 */
public interface IServerAPI {

    //region Call API
/*    @GET("appconfig")
    Call<ServerConfig> getConfig();

    @GET("conference/alive")
    Call<Conference> getAliveConfs();

    @POST("conference/launch")
    Call<HttpResult<Conference>> createConf(Conference conf);
    */

    //endregion

    //region RX API

    @GET("appconfig")
    Observable<ServerConfig> getConfigRx();

    @GET("conference/alive")
    Observable<HttpResult<Conference>> getAliveConfsRx();

    @POST("conference/launch")
    Observable<HttpResult<Conference>> createConfRx(@Body Conference conf);

    @GET
    Observable<String> getGitHubRx(@Url String url);
    //endregion
}

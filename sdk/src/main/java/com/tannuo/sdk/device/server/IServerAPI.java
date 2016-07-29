package com.tannuo.sdk.device.server;


import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
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
    Observable<HttpResult<List<Conference>>> getAliveConfsRx();

    @POST("user/wxlogin")
    Observable<HttpResult<User>> wxLoginRx(@Body User user);

    @POST("conference/launch")
    Observable<HttpMeetingResult<Conference>> createConfRx(@Body Conference conf);

    @POST("conference/update")
    Observable<HttpMeetingResult<Conference>> updateConfRx(@Body Conference conf);

    @POST("conference/join")
    Observable<HttpMeetingResult<Conference>> joinConfRx(@Body Conference conf);

    @POST("conference/end")
    Observable<HttpResult<Void>> endConfRx(@Body Conference conf);

    @POST
    Observable<Response<ResponseBody>> postConfDataRx(@Url String url, @Body RequestBody body);

    @GET
    Observable<Response<ResponseBody>> getConfDataRx(@Url String url);

    @POST
    Observable<Response<ResponseBody>> postImageRx(@Url String url, @Body RequestBody body);

    @POST("meeting/{meetingId}/heartbeat")
    Observable<HttpResult<Void>> postHeartbeat(@Path("meetingId") String meetingId);

    @GET("meeting/{meetingId}/iamonline/{userId}")
    Observable<HttpResult<Void>> getUserOnlineRx(@Path("meetingId") String meetingId, @Path("userId") String userId);

    @GET("meeting/{meetingId}/iamoffline/{userId}")
    Observable<HttpResult<Void>> getUserOfflineRx(@Path("meetingId") String meetingId, @Path("userId") String userId);

    //endregion
}

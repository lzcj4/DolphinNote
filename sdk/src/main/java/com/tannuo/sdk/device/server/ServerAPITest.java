package com.tannuo.sdk.device.server;


import android.text.TextUtils;

import com.tannuo.sdk.device.TouchFrame;
import com.tannuo.sdk.device.TouchFrameSet;
import com.tannuo.sdk.device.TouchPoint;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Subscriber;

/**
 * Created by Nick_PC on 2016/7/23.
 */
public class ServerAPITest {
    private ServerAPI mServerApi;

    public ServerAPITest() {
        mServerApi = ServerAPI.getInstance();
    }

    public void test() {
        testWXLogin(new DefaultSubscribe<User>() {
            @Override
            public void onNext(User user) {
                super.onNext(user);
                testCreateConf(user, new DefaultSubscribe<HttpMeetingResult<Conference>>() {
                            @Override
                            public void onNext(HttpMeetingResult<Conference> result) {
                                super.onNext(result);
                                if (!TextUtils.isEmpty(result.getMeetingUrl()) && result.getData() != null) {
                                    testPostConfData(result.getMeetingUrl(), result.getData().getId(),
                                            new DefaultSubscribe<Response<ResponseBody>>() {
                                                @Override
                                                public void onNext(Response<ResponseBody> responseBodyResponse) {
                                                    super.onNext(responseBodyResponse);
                                                    int code = responseBodyResponse.raw().code();
                                                    if (code == 200) {
                                                        testGetConfData(result.getMeetingUrl(), result.getData().getId(), -1);
                                                    }
                                                }
                                            });
                                }
                            }
                        }

                );
            }
        });
    }

    private void testWXLogin(Subscriber<User> subscriber) {
        User user = new User();
        user.setWeixinOpenId("id_abcd12355");
        user.setName("wx_NICK");
        user.setCity("杭州");
        user.setCountry("CHINA");
        user.setProvince("zj");
        mServerApi.toSubscribe(mServerApi.serverAPI.wxLoginRx(user)
                .map(new ServerAPI.HttpResultFunc()), subscriber);
    }

    private void testCreateConf(User user, Subscriber<HttpMeetingResult<Conference>> subscriber) {
        Conference conf = new Conference();
        conf.setName("CC_1");
        conf.setPassword("123");
        conf.setTechBridgeId("114893723");
        List<String> nickNames = new ArrayList<>();
        nickNames.add("Nick");
        conf.setNicknames(nickNames);
        List<String> userIds = new ArrayList<>();
        userIds.add(user.getId());
        conf.setUsers(userIds);
        conf.setDatetime(System.currentTimeMillis());
        mServerApi.toSubscribe(mServerApi.serverAPI.createConfRx(conf), subscriber);
        //.map(new ServerAPI.HttpResultFunc<>()), subscriber);
    }

    private void testPostConfData(String baseUrl, String meetingId, DefaultSubscribe<Response<ResponseBody>> subscriber) {
        TouchFrame frame = new TouchFrame();
        TouchPoint p = new TouchPoint();
        p.setId(11);
        p.setX((short) 22);
        p.setY((short) 33);
        p.setWidth((short) 44);
        p.setHeight((short) 55);
        frame.put(p);

        TouchPoint p2 = new TouchPoint();
        p2.setId(666);
        p2.setX((short) 777);
        p2.setY((short) 888);
        p2.setWidth((short) 999);
        p2.setHeight((short) 989);
        frame.put(p2);

        mServerApi.postConfData(baseUrl, meetingId, frame, subscriber);
    }

    private void testGetConfData(String baseUrl, String meetingId, int seqId) {

        mServerApi.getConfData(baseUrl, meetingId, seqId, new DefaultSubscribe<Response<ResponseBody>>() {
            @Override
            public void onNext(Response<ResponseBody> responseBodyResponse) {
                super.onNext(responseBodyResponse);

                int code = responseBodyResponse.raw().code();
                if (code == 200) {
                    DataInputStream reader = new DataInputStream(responseBodyResponse.body().byteStream());
                    try {
                        TouchFrameSet frame = TouchFrameSet.getFrames(reader);
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
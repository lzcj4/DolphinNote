package com.tannuo.sdk.device.protocol;

/**
 * Created by Nick_PC on 2016/7/1.
 */
public interface IProtocolFactory {

    IProtocol get(int type);
}

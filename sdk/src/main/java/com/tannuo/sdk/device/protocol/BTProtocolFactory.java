package com.tannuo.sdk.device.protocol;

import java.security.InvalidParameterException;

/**
 * Created by Nick_PC on 2016/7/1.
 */
public class BTProtocolFactory implements IProtocolFactory {

    private static IProtocolFactory instance;

    public static IProtocolFactory getInstance() {
        if (instance == null) {
            instance = new BTProtocolFactory();
        }
        return instance;
    }

    @Override
    public IProtocol get(int type) {
        IProtocol result = null;
        switch (type) {
            case ProtocolType.JY:
                result = new JYProtocol(new JYTouchScreen(600, 2000));
                break;
            case ProtocolType.PQ:
                break;
            case ProtocolType.CVT:
                result = new CVTProtocol();
                break;
            default:
                throw new InvalidParameterException();
        }
        return result;
    }
}

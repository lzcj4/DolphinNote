package com.tannuo.sdk.device.protocol;

/**
 * Created by Nick_PC on 2016/7/18.
 */
public class UsbProtocolFactory implements IProtocolFactory {
    @Override
    public IProtocol getProtocol(int type) {
        IProtocol result = null;
        switch (type) {
            case ProtocolType.JY:
                break;
            case ProtocolType.PQ:
                break;
            case ProtocolType.CVT:
                result = new CVTUsbProtocol();
                break;
            default:
                break;
        }
        return result;
    }
}

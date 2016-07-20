package com.tannuo.sdk.device.usb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Nick_PC on 2016/7/6.
 */
public class HttpServer extends NanoHTTPD {
    public HttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> params = session.getParms();
        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        String queryStr = session.getQueryParameterString();

        InputStream inputStream = session.getInputStream();
        InputStreamReader inputReader = new InputStreamReader(inputStream);

        return super.serve(session);
    }
}

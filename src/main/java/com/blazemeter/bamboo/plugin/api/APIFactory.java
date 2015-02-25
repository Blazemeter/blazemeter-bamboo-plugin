package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.ApiVersion;

/**
 * Created by zmicer on 25.2.15.
 */
public class APIFactory {
    private APIFactory(){}

    public static BlazemeterApi getAPI(String serverName,
                                       int serverPort, String username,
                                       String password,
                                       String blazeMeterApiVersion) {
        BlazemeterApi blazemeterAPI=null;
        switch (ApiVersion.valueOf(blazeMeterApiVersion)) {
            case autoDetect:
                blazemeterAPI = new BlazemeterApiV2Impl(serverName, serverPort, username, password);
                break;
            case v3:
                blazemeterAPI = new BlazemeterApiV2Impl(serverName, serverPort, username, password);
                break;
            case v2:
                blazemeterAPI = new BlazemeterApiV2Impl(serverName, serverPort, username, password);
                break;
        }
        return blazemeterAPI;
    }
}

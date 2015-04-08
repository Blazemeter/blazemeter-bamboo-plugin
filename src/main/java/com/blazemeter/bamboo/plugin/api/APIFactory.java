package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.ApiVersion;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by zmicer on 25.2.15.
 */
public class APIFactory {
    private APIFactory(){}

    public static BlazemeterApi getAPI(String serverUrl, String serverName,
                                       int serverPort, String username,
                                       String password,
                                       String blazeMeterApiVersion) {
        BlazemeterApi blazemeterAPI=null;
        switch (StringUtils.isBlank(blazeMeterApiVersion)
                ?ApiVersion.autoDetect:ApiVersion.valueOf(blazeMeterApiVersion)) {
            case autoDetect:
                blazemeterAPI = new BlazemeterApiV3Impl(serverUrl,serverName, serverPort, username, password);
                break;
            case v3:
                blazemeterAPI = new BlazemeterApiV3Impl(serverUrl,serverName, serverPort, username, password);
                break;
            case v2:
                blazemeterAPI = new BlazemeterApiV2Impl(serverUrl,serverName, serverPort, username, password);
                break;
        }
        return blazemeterAPI;
    }
}

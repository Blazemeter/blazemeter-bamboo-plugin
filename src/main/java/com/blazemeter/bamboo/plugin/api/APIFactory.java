package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.ApiVersion;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by zmicer on 25.2.15.
 */
public class APIFactory {
    private APIFactory(){}

    public static BlazemeterApi getAPI(String userKey, String serverUrl, String proxyServer,
                                       String proxyPort, String proxyUser,
                                       String proxyPassword,
                                       String blazeMeterApiVersion) {
        BlazemeterApi blazemeterAPI=null;
        int proxyPortInt= (StringUtils.isBlank(proxyPort)?0:Integer.parseInt(proxyPort));

        switch (StringUtils.isBlank(blazeMeterApiVersion)
                ?ApiVersion.autoDetect:ApiVersion.valueOf(blazeMeterApiVersion)) {
            case autoDetect:
                blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverUrl,proxyServer, proxyPortInt, proxyUser, proxyPassword);
                break;
            case v3:
                blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverUrl,proxyServer, proxyPortInt, proxyUser, proxyPassword);
                break;
            case v2:
                blazemeterAPI = new BlazemeterApiV2Impl(userKey,serverUrl,proxyServer, proxyPortInt, proxyUser, proxyPassword);
                break;
        }
        return blazemeterAPI;
    }
}

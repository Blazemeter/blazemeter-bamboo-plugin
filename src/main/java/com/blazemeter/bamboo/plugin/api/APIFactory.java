package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.ApiVersion;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by zmicer on 25.2.15.
 */
public class APIFactory {
    private APIFactory(){}

    public static BlazemeterApi getAPI(String userKey, String serverUrl,String blazeMeterApiVersion) {
        BlazemeterApi blazemeterAPI=null;

        switch (StringUtils.isBlank(blazeMeterApiVersion)
                ?ApiVersion.autoDetect:ApiVersion.valueOf(blazeMeterApiVersion)) {
            case autoDetect:
                blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverUrl);
                break;
            case v3:
                blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverUrl);
                break;
            case v2:
                blazemeterAPI = new BlazemeterApiV2Impl(userKey,serverUrl);
                break;
        }
        return blazemeterAPI;
    }
}

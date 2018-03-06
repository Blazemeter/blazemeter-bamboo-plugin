package com.blazemeter.bamboo.plugin.configuration;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.Utils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class BambooBzmUtils extends BlazeMeterUtils {

    private static final String BAMBOO_PLUGIN_INFO = "_clientId=CI_BAMBOO&_clientVersion=" + Utils.getVersion() + "&";

    public BambooBzmUtils(String apiKeyId, String apiKeySecret, String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, dataAddress, notifier, logger);
    }

    public BambooBzmUtils(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(address, dataAddress, notifier, logger);
    }

    @Override
    protected String modifyRequestUrl(String url) {
        return url + (url.contains("?") ? "&" : "?") + BAMBOO_PLUGIN_INFO;
    }

}

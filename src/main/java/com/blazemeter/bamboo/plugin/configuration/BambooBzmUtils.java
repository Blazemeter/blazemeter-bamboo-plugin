package com.blazemeter.bamboo.plugin.configuration;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.Utils;

public class BambooBzmUtils extends BlazeMeterUtils {


    public BambooBzmUtils(String apiKeyId, String apiKeySecret, String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, dataAddress, notifier, logger);
    }

    public BambooBzmUtils(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(address, dataAddress, notifier, logger);
    }

    @Override
    protected String modifyRequestUrl(String url) {
        return url+(url.contains("?")?"&":"?") + "_clientId=CI_BAMBOO&_clientVersion="+ Utils.getVersion() + "&";
    }
}

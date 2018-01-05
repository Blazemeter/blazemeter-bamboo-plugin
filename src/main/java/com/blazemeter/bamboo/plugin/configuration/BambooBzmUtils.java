package com.blazemeter.bamboo.plugin.configuration;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.Utils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class BambooBzmUtils extends BlazeMeterUtils {

    private static final String BAMBOO_PLUGIN_INFO="_clientId=CI_BAMBOO&_clientVersion="+ Utils.getVersion() + "&";

    public BambooBzmUtils(String apiKeyId, String apiKeySecret, String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, dataAddress, notifier, logger);
    }

    public BambooBzmUtils(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(address, dataAddress, notifier, logger);
    }

    @Override
    protected String modifyRequestUrl(String url) {
        return url+(url.contains("?")?"&":"?") + BAMBOO_PLUGIN_INFO;
    }

// TODO to api-client 1.3
    @Override
    protected String extractErrorMessage(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                JSONObject jsonResponse = JSONObject.fromObject(response);
                JSONObject errorObj = jsonResponse.getJSONObject("error");
                if (errorObj.containsKey("message")) {
                    return errorObj.getString("message");
                }
            } catch (JSONException ex) {
                logger.debug("Cannot parse response: " + response, ex);
                return "Cannot parse response: " + response;
            }
        }
        return null;
    }

}

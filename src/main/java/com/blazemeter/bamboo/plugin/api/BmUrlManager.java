package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by zmicer on 21.2.15.
 */
public interface BmUrlManager {
    String CLIENT_IDENTIFICATION = "_clientId=CI_BAMBOO&_clientVersion="
            + Utils.getVersion()+"&​";

    String testStatus(String appKey, String userKey, String testId);

    String scriptUpload(String appKey, String userKey, String testId, String fileName);

    String fileUpload(String appKey, String userKey, String testId, String fileName);

    String testStart(String appKey, String userKey, String testId);

    String testStop(String appKey, String userKey, String testId);

    String testReport(String appKey, String userKey, String reportId);

    String getServerUrl();

    String getTests(String appKey, String userKey);

    String getTresholds(String appKey, String userKey, String sessionId);

    String getTestConfig(String appKey, String userKey, String testId);

    String testTerminate(String appKey, String userKey, String testId);

    String testSessionStatus(String appKey, String userKey, String testId);
}
package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by zmicer on 21.2.15.
 */
public interface BmUrlManager {
    String CLIENT_IDENTIFICATION = "_clientId=CI_TEAMCITY&_clientVersion="
            + Utils.getVersion()+"&​";

    public String testStatus(String appKey, String userKey, String testId);

    public String scriptUpload(String appKey, String userKey, String testId, String fileName);

    public String fileUpload(String appKey, String userKey, String testId, String fileName);

    public String testStart(String appKey, String userKey, String testId);

    public String testStop(String appKey, String userKey, String testId);

    public String testReport(String appKey, String userKey, String reportId);

    public String getServerUrl();

    public String getUrlForTestList(String appKey, String userKey);
}
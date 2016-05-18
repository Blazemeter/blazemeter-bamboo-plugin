package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.ServiceManager;

/**
 * Created by zmicer on 21.2.15.
 */
public interface UrlManager {
    String CLIENT_IDENTIFICATION = "_clientId=CI_BAMBOO&_clientVersion="
            + ServiceManager.getVersion()+"&​";
    String LATEST="/api/latest";
    String TESTS="/tests";
    String MASTERS="/masters";
    String WEB="/web";


    String getServerUrl();

    void setServerUrl(String serverUrl);

    String masterStatus(String appKey, String userKey, String testId);

    String tests(String appKey, String userKey);

    String activeTests(String appKey, String userKey);

    String scriptUpload(String appKey, String userKey, String testId, String fileName);

    String fileUpload(String appKey, String userKey, String testId, String fileName);

    String testStart(String appKey, String userKey, String testId);

    String collectionStart(String appKey, String userKey, String collectionId);

    String masterStop(String appKey, String userKey, String testId);

    String testTerminate(String appKey, String userKey, String testId);

    String testReport(String appKey, String userKey, String reportId);

    String getUser(String appKey, String userKey);

    String ciStatus(String appKey, String userKey, String sessionId);

    String getTestConfig(String appKey, String userKey, String testId);

    String postJsonConfig(String appKey, String userKey, String testId);

    String createTest(String appKey, String userKey);

    String retrieveJUNITXML(String appKey, String userKey, String sessionId);

    String retrieveJTLZIP(String appKey, String userKey, String sessionId);

    String generatePublicToken(String appKey, String userKey, String sessionId);

    String listOfSessionIds(String appKey, String userKey, String masterId);

    String properties(String appKey, String userKey, String sessionId);

    String masterId(String appKey,String userKey, String masterId);

}
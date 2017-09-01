/**
 * Copyright 2016 BlazeMeter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlManagerV3Impl implements UrlManager {

    private String serverUrl = "";

    public UrlManagerV3Impl(String blazeMeterUrl) {
        this.serverUrl = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String masterStatus(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/status?events=false&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String tests(String appKey) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }/*
        TODO
        workspace id is required
        */
//        getTests= serverUrl +V4+"/tests?&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return serverUrl + "/api/web/tests?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStart(String appKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
            testId = URLEncoder.encode(testId, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + "/tests/" + testId + "/start?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String collectionStart(String appKey, String collectionId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
            collectionId = URLEncoder.encode(collectionId, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + "/collections/" + collectionId + "/start?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterStop(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
            masterId = URLEncoder.encode(masterId, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/stop?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testTerminate(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
            masterId = URLEncoder.encode(masterId, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/terminate?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
            masterId = URLEncoder.encode(masterId, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/reports/main/summary?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String getUser(String appKey) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + "/user?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String ciStatus(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/ci-status?app_key=" + appKey + CLIENT_IDENTIFICATION;

    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/reports/thresholds?format=junit&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String retrieveJTLZIP(String appKey, String sessionId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + "/sessions/" + sessionId + "/reports/logs?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String generatePublicToken(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/public-token?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String listOfSessionIds(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "/sessions?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String activeTests(String appKey) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*
        TODO
        /api/v4/masters?workspaceId=<>&active=true
         */
        return serverUrl + V4 + "/web/active?app_key=" + appKey + CLIENT_IDENTIFICATION;

    }

    @Override
    public String properties(String appKey, String sessionId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + "/sessions/" + sessionId + "/properties?target=all&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterId(String appKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + Constants.MASTERS + masterId + "?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testConfig(String appKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, UrlManager.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + V4 + "/tests/" + testId + "?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

}

/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.ServiceManager;

public interface UrlManager {
    String CLIENT_IDENTIFICATION = "&_clientId=CI_BAMBOO&_clientVersion="
            + ServiceManager.getVersion()+"&​";
    String V4 ="/api/v4";
    String TESTS="/tests";
    String MASTERS="/masters";
    String WEB="/web";


    String getServerUrl();

    void setServerUrl(String serverUrl);

    String masterStatus(String appKey, String testId);

    String tests(String appKey);

    String activeTests(String appKey);

    String testStart(String appKey, String testId);

    String collectionStart(String appKey, String collectionId);

    String masterStop(String appKey, String testId);

    String testTerminate(String appKey, String testId);

    String testReport(String appKey, String reportId);

    String getUser(String appKey);

    String ciStatus(String appKey, String sessionId);

    String retrieveJUNITXML(String appKey, String sessionId);

    String retrieveJTLZIP(String appKey, String sessionId);

    String generatePublicToken(String appKey, String sessionId);

    String listOfSessionIds(String appKey, String masterId);

    String properties(String appKey, String sessionId);

    String masterId(String appKey,String masterId);

    String testConfig(String appKey, String testId);
    }

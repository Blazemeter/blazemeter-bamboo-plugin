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

import com.blazemeter.bamboo.plugin.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


public interface Api {
    String APP_KEY = "bmboo0x98a8w9s4s7c4";

    TestStatus testStatus(String testId);

    String startTest(String testId, TestType testType) throws Exception;

    boolean active(String testId);

    int masterStatus(String id);

    JSONObject retrieveJtlZip(String sessionId) throws Exception;

    String retrieveJunit(String masterId) throws Exception;

    int getTestCount() throws Exception;

    boolean stopTest(String testId) throws JSONException;

    JSONObject testReport(String reportId);

    LinkedHashMultimap<String, String> getTestList() throws IOException;

    boolean verifyUserKey();

    JSONObject ciStatus(String sessionId);

    String getUserKey();

    void  setUserKey(String userKey);

    List<String> getListOfSessionIds(String masterId) throws Exception;

    JSONObject terminateTest(String testId);

    JSONObject getTestsJSON();

    String url();

    JSONObject publicToken(String masterId);

    boolean properties(JSONArray properties, String sessionId) throws Exception;

    boolean notes(String note,String masterId)throws Exception;

}


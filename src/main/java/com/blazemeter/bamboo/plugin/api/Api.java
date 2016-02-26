package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * 
 * @author 
 *
 */
public interface Api {
    String APP_KEY = "bmboo0x98a8w9s4s7c4";

    boolean uploadJmx(String testId, String fileName, String pathName);

    JSONObject uploadFile(String testId, String fileName, String pathName);

    TestStatus testStatus(String testId);

    String startTest(String testId) throws JSONException;

    int masterStatus(String id);

    int getTestCount() throws JSONException, IOException;

    boolean stopTest(String testId) throws JSONException;

    JSONObject testReport(String reportId);

    LinkedHashMultimap<String, String> getTestList() throws IOException;

    boolean verifyUserKey();

    JSONObject ciStatus(String sessionId);

    String getUserKey();

    void  setUserKey(String userKey);

    JSONObject putTestInfo(String testId, JSONObject data);

    JSONObject getTestConfig(String testId);

    JSONObject terminateTest(String testId);

}
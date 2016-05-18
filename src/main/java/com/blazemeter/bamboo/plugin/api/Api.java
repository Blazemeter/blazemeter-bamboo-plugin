package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


/**
 * 
 * @author 
 *
 */
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

    List<String> getListOfSessionIds(String masterId);

    JSONObject terminateTest(String testId);

    JSONObject getTestsJSON();

    String url();

    JSONObject publicToken(String masterId);

    boolean properties(JSONArray properties, String sessionId) throws Exception;

    boolean notes(String note,String masterId)throws Exception;

}


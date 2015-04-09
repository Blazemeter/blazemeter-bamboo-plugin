package com.blazemeter.bamboo.plugin.api;

import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * 
 * @author 
 *
 */
public interface BlazemeterApi {
    public static final String APP_KEY = "bmboo0x98a8w9s4s7c4";


    public boolean uploadJmx(String testId, String fileName, String pathName);

    public JSONObject uploadFile(String testId, String fileName, String pathName);

    public TestInfo getTestRunStatus(String testId);

    public String startTest(String testId) throws JSONException;

    public int getTestCount() throws JSONException, IOException;

    public boolean stopTest(String testId) throws JSONException;

    public JSONObject testReport(String reportId);

    public LinkedHashMultimap<String, String> getTestList() throws IOException;

    public boolean verifyUserKey();

    public JSONObject getTresholds(String sessionId);

    public String getUserKey();

    public void  setUserKey(String userKey);
}

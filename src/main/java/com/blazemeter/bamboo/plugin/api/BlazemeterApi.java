package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.Utils;
import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * 
 * @author 
 *
 */
public interface BlazemeterApi {

    public boolean uploadJmx(String userKey, String testId, String fileName, String pathName);

    public JSONObject uploadFile(String userKey, String testId, String fileName, String pathName);

    public TestInfo getTestRunStatus(String userKey, String testId);

    public JSONObject startTest(String userKey, String testId);

    public int getTestCount(String userKey) throws JSONException, IOException;

    public JSONObject stopTest(String userKey, String testId);

    public JSONObject aggregateReport(String userKey, String reportId);

    public HashMap<String, String> getTestList(String userKey) throws IOException;

}

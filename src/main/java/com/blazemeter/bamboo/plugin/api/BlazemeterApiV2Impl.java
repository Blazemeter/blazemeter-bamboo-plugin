package com.blazemeter.bamboo.plugin.api;

import java.io.IOException;
import java.io.PrintStream;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.Utils;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang3.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * @author 
 *
 */
public class BlazemeterApiV2Impl implements BlazemeterApi{
    PrintStream logger = new PrintStream(System.out);

    BzmHttpClient bzmHttpClient;
    BmUrlManagerV2Impl urlManager;
    private final JSONObject not_implemented;
    private String userKey;

	public BlazemeterApiV2Impl(String userKey, String serverUrl) {
    	this.userKey=userKey;
        urlManager = new BmUrlManagerV2Impl(serverUrl);
        not_implemented=new JSONObject();
        try {
            bzmHttpClient = new BzmHttpClient();
            not_implemented.put(Constants.NOT_IMPLEMENTED, Constants.NOT_IMPLEMENTED);
        } catch (Exception ex) {
            logger.format("Error while instantiating HTTPClient. Exception received: %s", ex);
        }
    }




    /**
     * @param testId   - test id
     * @param fileName - test name
     * @param pathName - jmx file path
     *                 //     * @return test id
     *                 //     * @throws java.io.IOException
     *                 //     * @throws org.json.JSONException
     */
    @Override
    public synchronized boolean uploadJmx(String testId, String fileName, String pathName) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return false;

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put(JsonConstants.DATA, fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
            return false;
        }

        this.bzmHttpClient.getResponseAsJson(url, jmxData,Method.GET);
        return true;
    }

    /**
     * @param testId   - test id
     * @return test id
     *         //     * @throws java.io.IOException
     *         //     * @throws org.json.JSONException
     */


    @Override
    public synchronized JSONObject uploadFile(String testId, String fileName, String pathName) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put(JsonConstants.DATA, fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return this.bzmHttpClient.getResponseAsJson(url, jmxData, Method.POST);
    }


    @Override
    public TestInfo getTestInfo(String testId) {
        TestInfo ti = new TestInfo();

        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId))
        {
            ti.status = TestStatus.NotFound.toString();
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);

            if (jo.get(JsonConstants.STATUS) == JsonConstants.TEST_NOT_FOUND)
                ti.status = TestStatus.NotFound.toString();
            else {
                ti.id = jo.getString(JsonConstants.TEST_ID);
                ti.name = jo.getString(JsonConstants.TEST_NAME);
                ti.status = jo.getString(JsonConstants.STATUS);
            }
        } catch (Exception e) {
            logger.println("error getting status " + e);
            ti.status = TestStatus.Error.toString();
        }
        return ti;
    }

    @Override
    public synchronized String startTest(String testId) throws JSONException{

        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(testId)) return null;
        String session = null;
        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        JSONObject json = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        if (!json.get(JsonConstants.RESPONSE_CODE).equals(200)) {
            if (json.get(JsonConstants.RESPONSE_CODE).equals(500) && json.get("error").toString().startsWith("Test already running")) {
                return "";
            }
        }
        session = json.get("session_id").toString();
        return session;
    }

    @Override
    public int getTestCount() throws JSONException, IOException {
        if (StringUtils.isBlank(userKey)) {
            logger.println("getTests userKey is empty");
            return 0;
        }

        String url = this.urlManager.getTests(APP_KEY, userKey);

        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
        if (!r.equals("200"))
            return 0;
        JSONArray arr = (JSONArray) jo.get(JsonConstants.TESTS);
        return arr.length();
    }


    /**
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public boolean stopTest(String testId) throws JSONException{
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return false;
        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, Method.GET).get(JsonConstants.RESPONSE_CODE).equals(200);
    }

    /**
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = (JSONObject) this.bzmHttpClient.getResponseAsJson(url, null, Method.GET).getJSONObject(JsonConstants.RESULT)
                .getJSONArray("summary")
                .get(0);
        return summary;
    }


    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException {

        LinkedHashMultimap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
            logger.println("getTests userKey is empty");
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            logger.println(url);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
            try {
                String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get(JsonConstants.TESTS);
                    testListOrdered = LinkedHashMultimap.create(arr.length(),arr.length());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject en = null;
                        try {
                            en = arr.getJSONObject(i);
                        } catch (JSONException e) {
                            logger.println("Error with the JSON while populating test list, " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (en != null) {
                                id = en.getString(JsonConstants.TEST_ID);
                                name = en.getString(JsonConstants.TEST_NAME).replaceAll("&", "&amp;");
                                testListOrdered.put(id, name);

                            }
                        } catch (JSONException ie) {
                            logger.println("Error with the JSON while populating test list, " + ie);
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.println("Error while populating test list, " + e);
            }
        }

        return testListOrdered;
    }

    @Override
    public boolean verifyUserKey() {

        if (userKey == null || userKey.trim().isEmpty()) {
            return false;
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            try {
            	JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
                String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
                if (r.equals("200")) {
                    return true;
                } else {
                	return false;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
    }

    
    @Override
    public JSONObject getTresholds(String sessionId) {
        return not_implemented;
    }

    @Override
    public String getUserKey() {
        return this.userKey;
    }

    @Override
    public void setUserKey(String userKey) {
        this.userKey=userKey;
    }

    @Override
    public JSONObject putTestInfo(String testId, JSONObject data) {
        return  not_implemented;
    }

    @Override
    public JSONObject getTestConfig(String testId) {
        return not_implemented;
    }

    @Override
    public int getTestSessionStatusCode(String id) {
        return 0;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        return not_implemented;
    }
}

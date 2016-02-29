package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * 
 * @author 
 *
 */
public class ApiV3Impl implements Api {
    private static final Logger logger = Logger.getLogger(ApiV3Impl.class);

    public static final String APP_KEY = "bmboo0x98a8w9s4s7c4";
    HttpWrapper http;
    UrlManagerV3Impl urlManager;
    private String userKey;

	public ApiV3Impl(String userKey, String serverUrl) {
    	this.userKey=userKey;
        urlManager = new UrlManagerV3Impl(serverUrl);
        try {
            http = new HttpWrapper();
        } catch (Exception ex) {
            logger.error("Failed to create api for communication with server: %s", ex);
        }
    }


    @Override
    public TestStatus testStatus(String id) throws JSONException{
        TestStatus testStatus = null;

        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(id)) {
            testStatus = TestStatus.NotFound;
            return testStatus;
        }

        try {
            String url = this.urlManager.masterStatus(APP_KEY, userKey, id);
            JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has("status") && !result.getString("status").equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    logger.error("Master "+id+ " is not running on server");
                    if (result.has("errors") && !result.get("errors").equals(JSONObject.NULL)) {
                        logger.error("MasterId: "+id+" -> error received from server: " + result.get("errors").toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("MasterId: "+id+" ->error getting status ", e);
            testStatus = TestStatus.Error;
        }
        return testStatus;
    }

    @Override
    public JSONObject getTestsJSON() {
        String url = this.urlManager.tests(APP_KEY, userKey);
        JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class);
        return jo;
    }


    @Override
    public synchronized String startTest(String testId, TestType testType) throws JSONException {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(testId)) return null;
        String url = "";
        switch (testType) {
            case multi:
                url = this.urlManager.collectionStart(APP_KEY, userKey, testId);
                break;
            default:
                url = this.urlManager.testStart(APP_KEY, userKey, testId);
        }
        JSONObject jo = this.http.response(url, null, Method.POST, JSONObject.class);

        if (jo==null) {
            if (logger.isDebugEnabled())
                logger.debug("Received NULL from server while start operation: will do 5 retries");
            boolean isActive=this.active(testId);
            if(!isActive){
                int retries = 1;
                while (retries < 6) {
                    try {
                        if (logger.isDebugEnabled())
                            logger.debug("Trying to repeat start request: " + retries + " retry.");
                        logger.debug("Pausing thread for " + 10*retries + " seconds before doing "+retries+" retry.");
                        Thread.sleep(10000*retries);
                        jo = this.http.response(url, null, Method.POST, JSONObject.class);
                        if (jo!=null) {
                            break;
                        }
                    } catch (InterruptedException ie) {
                        if (logger.isDebugEnabled())
                            logger.debug("Start operation was interrupted at pause during " + retries + " request retry.");
                    } catch (Exception ex) {
                        if (logger.isDebugEnabled())
                            logger.debug("Received bad response from server while starting test: " + retries + " retry.");
                    }
                    finally {
                        retries++;
                    }
                }


            }
        }
        JSONObject result=null;
        try{
            result = (JSONObject) jo.get(JsonConstants.RESULT);
        }catch (Exception e){
            if (logger.isDebugEnabled())
                logger.debug("Error while starting test: ",e);
            throw new JSONException("Faild to get 'result' node "+e.getMessage());

        }
        return String.valueOf(result.getInt(JsonConstants.ID));
    }


        @Override
        public boolean active(String testId) {
            boolean isActive=false;
            String url = this.urlManager.activeTests(APP_KEY, userKey);
            JSONObject jo = null;
            try {
                jo = this.http.response(url, null, Method.GET, JSONObject.class);
                JSONObject result = null;
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONObject) jo.get(JsonConstants.RESULT);
                    JSONArray tests = (JSONArray) result.get(JsonConstants.TESTS);
                    for(int i=0;i<tests.length();i++){
                        if(String.valueOf(tests.getInt(i)).equals(testId)){
                            isActive=true;
                            return isActive;
                        }
                    }
                    JSONArray collections = (JSONArray) result.get(JsonConstants.COLLECTIONS);
                    for(int i=0;i<collections.length();i++){
                        if(String.valueOf(collections.getInt(i)).equals(testId)){
                            isActive=true;
                            return isActive;
                        }
                    }
                }
                return isActive;
            } catch (JSONException je) {
                logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, je);
                return false;
            } catch (Exception e) {
                logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
                return false;
            }
        }


    @Override
    public int getTestCount() throws JSONException, IOException {
        if (StringUtils.isBlank(userKey)) {
            logger.error("UserKey is empty! Please, check settings.");
            return 0;
        }

        String url = this.urlManager.tests(APP_KEY, userKey);

        JSONObject jo = this.http.response(url, null,Method.GET,JSONObject.class);
        String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
        if (!r.equals("200"))
            return 0;
        JSONArray arr = (JSONArray) jo.get(JsonConstants.TESTS);
        return arr.length();
    }

    @Override
    public boolean stopTest(String masterId) throws JSONException{
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(masterId)) return false;
        logger.info("Stopping test with masterId="+masterId);
        String url = this.urlManager.masterStop(APP_KEY, userKey, masterId);
        JSONArray stopArray=this.http.response(url, null,Method.POST,JSONObject.class).getJSONArray(JsonConstants.RESULT);
        String command=((JSONObject)stopArray.get(0)).getString(JsonConstants.RESULT);
        return command.equals("shutdown command sent\n");
    }

    @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = (JSONObject) this.http.response(url, null, Method.GET,JSONObject.class).getJSONObject(JsonConstants.RESULT)
                .getJSONArray("summary")
                .get(0);
        return summary;
    }

    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException {

        LinkedHashMultimap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
            logger.error("UserKey is empty! Please, check settings.");
        } else {
            String url = this.urlManager.tests(APP_KEY, userKey);
            logger.info("Requesting url -> "+url);
            JSONObject jo = this.http.response(url, null,Method.GET,JSONObject.class);
            try {
                JSONArray arr = (JSONArray) jo.get(JsonConstants.RESULT);
                if (arr.length() > 0) {
                    testListOrdered = LinkedHashMultimap.create(arr.length(),arr.length());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject en = null;
                        try {
                            en = arr.getJSONObject(i);
                        } catch (JSONException e) {
                            logger.error("Error occured while parsing JSON in getTestList() operation, " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (en != null) {
                                id = String.valueOf(en.getInt(JsonConstants.ID));
                                name = en.getString(JsonConstants.NAME).replaceAll("&", "&amp;");
                                testListOrdered.put(id, name);

                            }
                        } catch (JSONException ie) {
                            logger.error("Error occured while populating test list, " + ie);
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.error("Error occured while populating test list, " + e);
            }
        }

        return testListOrdered;
    }

    @Override
    public boolean verifyUserKey() {

        if (userKey == null || userKey.trim().isEmpty()) {
            return false;
        } else {
            String url = this.urlManager.tests(APP_KEY, userKey);
            try {
                JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class);
                if (((JSONArray) jo.get(JsonConstants.RESULT)).length() > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    public JSONObject ciStatus(String sessionId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            return null;
        }
        String url = this.urlManager.ciStatus(APP_KEY, userKey, sessionId);
        JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class);
        return jo;
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
        if(StringUtils.isBlank(this.userKey)& StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, this.userKey, testId);
        JSONObject jo = this.http.response(url, data, Method.PUT,JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject getTestConfig(String testId){
        if(StringUtils.isBlank(this.userKey)& StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, this.userKey, testId);
        JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        if(StringUtils.isBlank(this.userKey)& StringUtils.isBlank(testId)) return null;
        String url = this.urlManager.testTerminate(APP_KEY, this.userKey, testId);
        return this.http.response(url, null, Method.POST,JSONObject.class);

    }


    @Override
    public int masterStatus(String id) {
        int statusCode=0;
        if(StringUtils.isBlank(this.userKey)& StringUtils.isBlank(id))
        {
            return statusCode;
        }
        try {
            String url = this.urlManager.masterStatus(APP_KEY, this.userKey, id);
            JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode=result.getInt(JsonConstants.PROGRESS);
        } catch (Exception e) {
        }finally {
            {
                return statusCode;
            }
        }
    }
}

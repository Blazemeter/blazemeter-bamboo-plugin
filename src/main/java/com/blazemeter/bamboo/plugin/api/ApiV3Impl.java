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
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
            logger.error("Either userKey or masterId value was empty while getting test status: setting TestStatus=TestStatus.NotFound");
            testStatus = TestStatus.NotFound;
            return testStatus;
        }

        try {
            logger.info("Trying to get test status for masterId = "+id);
            String url = this.urlManager.masterStatus(APP_KEY, userKey, id);
            JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class,JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) {
                logger.error("Test with masterId = "+id+" was not found on server.");
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has("status") && !result.getString("status").equals("ENDED")) {
                    logger.info("Test with masterId = "+id+" is running on server.");
                    testStatus = TestStatus.Running;
                } else {
                    logger.error("Master "+id+ " is not running on server");
                    if (result.has("errors") && !result.get("errors").equals(JSONObject.NULL)) {
                        logger.error("Test with masterId = "+id+" -> error received from server: " + result.get("errors").toString());
                        testStatus = TestStatus.Error;
                    } else {
                        logger.info("Test with masterId = "+id+" is not running on server.");
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
        if (StringUtils.isBlank(userKey)){
            logger.error("UserKey was empty while getting testList: returning null object.");
            return null;
        }
        JSONObject jo=null;
        try{
            logger.info("Trying to get tests...");
            String url = this.urlManager.tests(APP_KEY, userKey);
            jo = this.http.response(url, null, Method.GET,JSONObject.class,JSONObject.class);
        }catch (Exception e){
            logger.error("Got an exception while trying to get tests from server: "+e);
        }finally {
            return jo;
        }
     }


    @Override
    public synchronized String startTest(String testId, TestType testType) throws Exception {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(testId)) return null;
        String url = "";
        logger.info("Trying to start test with testId = "+testId+", testType = "+testType.name());
        switch (testType) {
            case multi:
                url = this.urlManager.collectionStart(APP_KEY, userKey, testId);
                break;
            default:
                url = this.urlManager.testStart(APP_KEY, userKey, testId);
        }
        JSONObject jo=null;
        try{
           jo = this.http.response(url, null, Method.POST, JSONObject.class,JSONObject.class);
        }catch (Exception e){
            logger.error("Failed to start test due to error: ",e);
            logger.error("Check server & proxy settings");
            throw e;
        }

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
                        jo = this.http.response(url, null, Method.POST, JSONObject.class,JSONObject.class);
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
            logger.info("Got result: "+result.toString());
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
            logger.info("Checking if test with testId = "+testId+" is active.");
            String url = this.urlManager.activeTests(APP_KEY, userKey);
            JSONObject jo = null;
            try {
                jo = this.http.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
                JSONObject result = null;
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONObject) jo.get(JsonConstants.RESULT);
                    logger.info("Got result: "+result.toString());
                    JSONArray tests = (JSONArray) result.get(JsonConstants.TESTS);
                    for(int i=0;i<tests.length();i++){
                        if(String.valueOf(tests.getInt(i)).equals(testId)){
                            logger.info("Test with testId = "+testId+" is active.");
                            isActive=true;
                            return isActive;
                        }
                    }
                    JSONArray collections = (JSONArray) result.get(JsonConstants.COLLECTIONS);
                    for(int i=0;i<collections.length();i++){
                        if(String.valueOf(collections.getInt(i)).equals(testId)){
                            logger.info("Test collection with testId = "+testId+" is active.");
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
    public int getTestCount() throws Exception {
        if (StringUtils.isBlank(userKey)) {
            logger.error("UserKey was empty while getting number of tests: returning '0'.");
            return 0;
        }
        String url = this.urlManager.tests(APP_KEY, userKey);
        JSONObject jo=null;
        try{
           jo = this.http.response(url, null,Method.GET,JSONObject.class,JSONObject.class);
        }catch (Exception e){
            logger.error("Failed to start test due to error: ",e);
            logger.error("Check server & proxy settings");
        }
        if(jo==null){
            return 0;
        }
        JSONArray arr = (JSONArray) jo.get(JsonConstants.RESULT);
        int testNum=arr.length();
        logger.error("Found "+testNum+" tests on server");
        return arr.length();
    }

    @Override
    public boolean stopTest(String masterId) throws JSONException{
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(masterId)){
            logger.error("UserKey was empty while stopping test: returning 'false'.");
            return false;
        }
        logger.info("Stopping test with masterId="+masterId);
        String url = this.urlManager.masterStop(APP_KEY, userKey, masterId);
        JSONArray stopArray=null;
        try {
            stopArray=this.http.response(url, null,Method.POST,JSONObject.class,JSONObject.class).getJSONArray(JsonConstants.RESULT);
        }catch (Exception e){
            logger.error("Failed to start test due to error: ",e);
            logger.error("Check server & proxy settings");
        }
        logger.info("Got stopArray: "+stopArray.toString());
        String command=((JSONObject)stopArray.get(0)).getString(JsonConstants.RESULT);
        return command.equals("shutdown command sent\n");
    }

    @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(reportId)) {
            logger.error("UserKey was empty while getting test report: returning 'null'.");
            return null;
        }

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = null;
        JSONObject jo = null;
        try {
            jo = this.http.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
            summary = (JSONObject) jo.getJSONObject(JsonConstants.RESULT)
                    .getJSONArray("summary")
                    .get(0);

            logger.info("Got summary: " + summary.toString());
        } catch (JSONException e) {
            logger.error("Problems with getting aggregate test report...", e);
        } catch (Exception e) {
            logger.error("Problems with getting aggregate test report...", e);
        }
        return summary;
    }

    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException {

        LinkedHashMultimap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
            logger.error("UserKey was empty while getting testsList.");
        } else {
            String url = this.urlManager.tests(APP_KEY, userKey);
            logger.info("Requesting url -> "+url);
            try {
                JSONObject jo = this.http.response(url, null,Method.GET,JSONObject.class,JSONObject.class);
                JSONArray arr = (JSONArray) jo.get(JsonConstants.RESULT);
                logger.info("Got result: "+arr.toString());
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
            logger.info("Verifying userKey...");
            String url = this.urlManager.tests(APP_KEY, userKey);
            try {
                JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class,JSONObject.class);
                JSONArray result=(JSONArray)jo.get(JsonConstants.RESULT);
                logger.info("Got result: "+result.toString());
                if ((result).length() > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                logger.error("Got an exception while verifying userKey: "+e);
                return false;
            }
        }
    }

    @Override
    public JSONObject ciStatus(String sessionId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            return null;
        }
        JSONObject jo=null;
        try {
            String url = this.urlManager.ciStatus(APP_KEY, userKey, sessionId);
            jo = this.http.response(url, null, Method.GET,JSONObject.class,JSONObject.class);
        }catch (Exception e){
            logger.error("Got an exception while getting ci status: "+e);
        }
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
    public JSONObject terminateTest(String testId) {
        if (StringUtils.isBlank(this.userKey) & StringUtils.isBlank(testId)) return null;
        String url = this.urlManager.testTerminate(APP_KEY, this.userKey, testId);
        JSONObject jo = null;
        try {
            jo = this.http.response(url, null, Method.POST, JSONObject.class,JSONObject.class);
        } catch (Exception e) {
            logger.error("Failed to start test due to error: ", e);
            logger.error("Check server & proxy settings");
        }
        return jo;
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
            JSONObject jo = this.http.response(url, null, Method.GET,JSONObject.class,JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode=result.getInt(JsonConstants.PROGRESS);
        } catch (Exception e) {
        }finally {
            {
                return statusCode;
            }
        }
    }

    @Override
    public String url(){
        return urlManager.getServerUrl();
    }

    @Override
    public JSONObject publicToken(String masterId) {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(masterId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, userKey, masterId);
        JSONObject jo = null;
        try {
            jo = this.http.response(url, null, Method.POST, JSONObject.class,JSONObject.class);
        } catch (Exception e) {
            logger.error("Failed to start test due to error: ", e);
            logger.error("Check server & proxy settings");
        }
        return jo;
    }


    @Override
    public List<String> getListOfSessionIds(String masterId) {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, userKey, masterId);
        JSONObject jo =  null;
        try {
            jo=this.http.response(url, null, Method.GET, JSONObject.class,JSONObject.class);

            JSONArray sessions = jo.getJSONObject(JsonConstants.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConstants.ID));
            }
        } catch (JSONException je) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, je);
        } catch (Exception e) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, e);
        } finally {
            return sessionsIds;
        }
    }



        @Override
        public JSONObject retrieveJtlZip(String sessionId) throws Exception {
            if (StringUtils.isBlank(userKey) & StringUtils.isBlank(sessionId)) return null;
            logger.info("Trying to get JTLZIP url for the sessionId=" + sessionId);
            String url = this.urlManager.retrieveJTLZIP(APP_KEY, userKey, sessionId);
            logger.info("Trying to retrieve JTLZIP json for the sessionId=" + sessionId);
            JSONObject jtlzip = this.http.response(url, null, Method.GET, JSONObject.class,JSONObject.class);
            return jtlzip;
        }

    @Override
    public String retrieveJunit(String masterId) throws Exception {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(masterId)) return null;
        logger.info("Trying to get Junit url for the masterId=" + masterId);
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, userKey, masterId);
        logger.info("Trying to retrieve Junit for the masterId=" + masterId);
        String junit = this.http.response(url, null, Method.GET, String.class,JSONObject.class);
        return junit;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) throws Exception {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(sessionId)) return false;
        String url = this.urlManager.properties(APP_KEY, userKey, sessionId);
        JSONObject jo = this.http.response(url, properties, Method.POST, JSONObject.class,JSONArray.class);
        try {
            if (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report properties to sessionId=" + sessionId, e);
        }
        return true;
    }

    @Override
    public boolean notes(String note, String masterId) throws Exception {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(masterId)) return false;
        String noteEsc = StringEscapeUtils.escapeJson("{'"+ JsonConstants.NOTE+"':'"+note+"'}");
        JSONObject noteJson=new JSONObject(noteEsc);
        String url = this.urlManager.masterId(APP_KEY, userKey, masterId);
        JSONObject jo=this.http.response(url, noteJson, Method.PATCH, JSONObject.class,JSONObject.class);
        try{
            if(!jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                return false;
            }}catch (Exception e){
            throw new Exception("Failed to submit report notest to masterId="+masterId,e);
        }
        return true ;
    }
}

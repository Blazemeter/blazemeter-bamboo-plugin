package com.blazemeter.bamboo.plugin.api;

import java.io.IOException;
import java.io.PrintStream;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.Utils;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonNodes;
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

    public static final String APP_KEY = "bmboo0x98a8w9s4s7c4";
    BzmHttpClient bzmHttpClient;
    BmUrlManagerV2Impl urlManager;
    private final JSONObject not_implemented;
    private String userKey;
	private String serverName;
	private int serverPort;
	private String username;
	private String password;
	
	public BlazemeterApiV2Impl(String userKey, String serverUrl, String serverName, int serverPort, String username, String password) {
    	this.userKey=userKey;
        this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;		
        urlManager = new BmUrlManagerV2Impl(serverUrl);
        not_implemented=new JSONObject();
        try {
            bzmHttpClient = new BzmHttpClient(serverName, username, password,serverPort);
            bzmHttpClient.configureProxy();
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
            jmxData.put(JsonNodes.DATA, fileCon);
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
            jmxData.put(JsonNodes.DATA, fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return this.bzmHttpClient.getResponseAsJson(url, jmxData, Method.POST);
    }


    @Override
    public TestInfo getTestRunStatus(String testId) {
        TestInfo ti = new TestInfo();

        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId))
        {
            ti.status = TestStatus.NotFound.toString();
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);

            if (jo.get(JsonNodes.STATUS) == JsonNodes.TEST_NOT_FOUND)
                ti.status = TestStatus.NotFound.toString();
            else {
                ti.id = jo.getString(JsonNodes.TEST_ID);
                ti.name = jo.getString(JsonNodes.TEST_NAME);
                ti.status = jo.getString(JsonNodes.STATUS);
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
        if (!json.get(JsonNodes.RESPONSE_CODE).equals(200)) {
            if (json.get(JsonNodes.RESPONSE_CODE).equals(500) && json.get("error").toString().startsWith("Test already running")) {
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
        String r = jo.get(JsonNodes.RESPONSE_CODE).toString();
        if (!r.equals("200"))
            return 0;
        JSONArray arr = (JSONArray) jo.get(JsonNodes.TESTS);
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
        return this.bzmHttpClient.getResponseAsJson(url, null, Method.GET).get(JsonNodes.RESPONSE_CODE).equals(200);
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
        return this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
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
                String r = jo.get(JsonNodes.RESPONSE_CODE).toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get(JsonNodes.TESTS);
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
                                id = en.getString(JsonNodes.TEST_ID);
                                name = en.getString(JsonNodes.TEST_NAME).replaceAll("&", "&amp;");
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
                String r = jo.get(JsonNodes.RESPONSE_CODE).toString();
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

    
    
    public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
}

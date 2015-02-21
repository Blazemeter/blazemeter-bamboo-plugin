package com.blazemeter.bamboo.plugin.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.blazemeter.bamboo.plugin.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * @author 
 *
 */
public class BlazemeterApi {
    PrintStream logger = new PrintStream(System.out);

    public static final String APP_KEY = "bmboo0x98a8w9s4s7c4";
    BzmHttpClient bzmHttpClient;
    BmUrlManager urlManager;

	private String serverName;
	private int serverPort;
	private String username;
	private String password;
	
	public BlazemeterApi(String serverName, int serverPort, String username, String password) {
    	this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;		
        urlManager = new BmUrlManager("https://a.blazemeter.com");
        try {
            bzmHttpClient = new BzmHttpClient();
            bzmHttpClient.configureProxy(serverName, serverPort, username, password);
        } catch (Exception ex) {
            logger.format("error Instantiating HTTPClient. Exception received: %s", ex);
        }
    }




    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - test name
     * @param pathName - jmx file path
     *                 //     * @return test id
     *                 //     * @throws java.io.IOException
     *                 //     * @throws org.json.JSONException
     */
    public synchronized boolean uploadJmx(String userKey, String testId, String fileName, String pathName) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return false;

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
            return false;
        }

        this.bzmHttpClient.getJson(url, jmxData);
        return true;
    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @return test id
     *         //     * @throws java.io.IOException
     *         //     * @throws org.json.JSONException
     */


    public synchronized JSONObject uploadFile(String userKey, String testId, String fileName, String pathName) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return this.bzmHttpClient.getJson(url, jmxData);
    } 


    public TestInfo getTestRunStatus(String userKey, String testId) {
        TestInfo ti = new TestInfo();

        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId))
        {
            ti.status = BlazeMeterConstants.TestStatus.NotFound;
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = this.bzmHttpClient.getJson(url, null);

            if (jo.get("status") == "Test not found")
                ti.status = BlazeMeterConstants.TestStatus.NotFound;
            else {
                ti.id = jo.getString("test_id");
                ti.name = jo.getString("test_name");
                ti.status = jo.getString("status");
            }
        } catch (Exception e) {
            logger.println("error getting status " + e);
            ti.status = BlazeMeterConstants.TestStatus.Error;
        }
        return ti;
    }

    public synchronized JSONObject startTest(String userKey, String testId) {

        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getJson(url, null);
    }

    public int getTestCount(String userKey) throws JSONException, IOException {
        if (StringUtils.isBlank(userKey)) {
            logger.println("getTests userKey is empty");
            return 0;
        }

        String url = getUrlForTestList(APP_KEY, userKey);

        JSONObject jo = this.bzmHttpClient.getJson(url, null);
        String r = jo.get("response_code").toString();
        if (!r.equals("200"))
            return 0;
        JSONArray arr = (JSONArray) jo.get("tests");
        return arr.length();
    }

    private String getUrlForTestList(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("https://a.blazemeter.com/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all", appKey, userKey);

    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    public JSONObject stopTest(String userKey, String testId) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getJson(url, null);
    }

    /**
     * @param userKey  - user key
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    public JSONObject aggregateReport(String userKey, String reportId) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testAggregateReport(APP_KEY, userKey, reportId);
        return this.bzmHttpClient.getJson(url, null);
    }

    public HashMap<String, String> getTestList(String userKey) throws IOException {

        LinkedHashMap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
            logger.println("getTests userKey is empty");
        } else {
            String url = getUrlForTestList(APP_KEY, userKey);
            logger.println(url);
            JSONObject jo = this.bzmHttpClient.getJson(url, null);
            try {
                String r = jo.get("response_code").toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get("tests");
                    testListOrdered = new LinkedHashMap<String, String>(arr.length());
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
                                id = en.getString("test_id");
                                name = en.getString("test_name").replaceAll("&", "&amp;");
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

    /*
    TODO
    This method should be refactored to interface.
    In current state it is ugly.
     */
    public boolean verifyUserKey(String userKey) {

        if (userKey == null || userKey.trim().isEmpty()) {
            return false;
        } else {
            String url = getUrlForTestList(APP_KEY, userKey);
            try {
            	JSONObject jo = this.bzmHttpClient.getJson(url, null);
                String r = jo.get("response_code").toString();
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

}

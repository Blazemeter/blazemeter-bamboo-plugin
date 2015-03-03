package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.Utils;
import com.blazemeter.bamboo.plugin.configuration.JsonNodes;
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
public class BlazemeterApiV3Impl implements BlazemeterApi{
    PrintStream logger = new PrintStream(System.out);

    public static final String APP_KEY = "bmboo0x98a8w9s4s7c4";
    BzmHttpClient bzmHttpClient;
    BmUrlManagerV3Impl urlManager;

	private String serverName;
	private int serverPort;
	private String username;
	private String password;

	public BlazemeterApiV3Impl(String serverName, int serverPort, String username, String password) {
    	this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;		
        urlManager = new BmUrlManagerV3Impl("https://a.blazemeter.com");
        try {
            bzmHttpClient = new BzmHttpClient(serverName, username, password, serverPort);
            bzmHttpClient.configureProxy();
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
    @Override
    public synchronized boolean uploadJmx(String userKey, String testId, String fileName, String pathName) {
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

        this.bzmHttpClient.getResponseAsJson(url,jmxData,Method.GET);
        return true;
    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @return test id
     *         //     * @throws java.io.IOException
     *         //     * @throws org.json.JSONException
     */


    @Override
    public synchronized JSONObject uploadFile(String userKey, String testId, String fileName, String pathName) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put(JsonNodes.DATA, fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return this.bzmHttpClient.getResponseAsJson(url, jmxData,Method.GET);
    }


    @Override
    public TestInfo getTestRunStatus(String userKey,String sessionId) throws JSONException{
        TestInfo ti = new TestInfo();
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(sessionId)) {
            ti.setStatus(TestStatus.NotFound.toString());
            return ti;
        }
        String url = this.urlManager.testStatus(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        JSONObject result = (JSONObject) jo.get(JsonNodes.RESULT);
        if (result.get(JsonNodes.DATA_URL) == null) {
            ti.setStatus(TestStatus.NotFound.toString());
        } else {
            ti.setId(String.valueOf(result.getInt("testId")));
            ti.setName(result.getString(JsonNodes.NAME));
            if (result.getString(JsonNodes.STATUS).equals("DATA_RECIEVED")) {
                ti.setStatus(TestStatus.Running.toString());
            } else if (result.getString(JsonNodes.STATUS).equals("ENDED")) {
                ti.setStatus(TestStatus.NotRunning.toString());
            } else {
                ti.setStatus(TestStatus.NotRunning.toString());
            }
        }
        return ti;
    }


    @Override
    public synchronized String startTest(String userKey, String testId) {
        String session=null;
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        JSONObject json=this.bzmHttpClient.getResponseAsJson(url, null,Method.GET);
        session = json.getJSONObject(JsonNodes.RESULT).getJSONArray("sessionsId").getString(0);
        return session;
    }

    @Override
    public int getTestCount(String userKey) throws JSONException, IOException {
        if (StringUtils.isBlank(userKey)) {
            logger.println("getTests userKey is empty");
            return 0;
        }

        String url = this.urlManager.getTests(APP_KEY, userKey);

        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null,Method.GET);
        String r = jo.get(JsonNodes.RESPONSE_CODE).toString();
        if (!r.equals("200"))
            return 0;
        JSONArray arr = (JSONArray) jo.get(JsonNodes.TESTS);
        return arr.length();
    }


    /**
     * @param userKey - user key
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public boolean stopTest(String userKey, String testId) throws JSONException{
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return false;
        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        JSONArray stopArray=this.bzmHttpClient.getResponseAsJson(url, null,Method.GET).getJSONArray(JsonNodes.RESULT);
        return stopArray.length()==0;
    }

    /**
     * @param userKey  - user key
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String userKey, String reportId) {
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = (JSONObject) this.bzmHttpClient.getResponseAsJson(url, null, Method.GET).getJSONObject(JsonNodes.RESULT)
                .getJSONArray("summary")
                .get(0);
        return summary;
    }

    @Override
    public HashMap<String, String> getTestList(String userKey) throws IOException {

        LinkedHashMap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
            logger.println("getTests userKey is empty");
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            logger.println(url);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null,Method.GET);
            try {
                JSONArray arr = (JSONArray) jo.get(JsonNodes.RESULT);
                if (arr.length() > 0) {
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
                                id = String.valueOf(en.getInt(JsonNodes.ID));
                                name = en.getString(JsonNodes.NAME).replaceAll("&", "&amp;");
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
    public boolean verifyUserKey(String userKey) {

        if (userKey == null || userKey.trim().isEmpty()) {
            return false;
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            try {
                JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
                if (((JSONArray) jo.get(JsonNodes.RESULT)).length() > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
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

package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.Utils;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintStream;


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
    private String userKey;
	private String serverName;
	private int serverPort;
	private String username;
	private String password;

	public BlazemeterApiV3Impl(String userKey, String serverUrl, String serverName, int serverPort, String username, String password) {
    	this.userKey=userKey;
        this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;		
        urlManager = new BmUrlManagerV3Impl(serverUrl);
        try {
            bzmHttpClient = new BzmHttpClient(serverName, username, password, serverPort);
            bzmHttpClient.configureProxy();
        } catch (Exception ex) {
            logger.format("error Instantiating HTTPClient. Exception received: %s", ex);
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

        this.bzmHttpClient.getResponseAsJson(url,jmxData,Method.GET);
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

        return this.bzmHttpClient.getResponseAsJson(url, jmxData,Method.GET);
    }


    @Override
    public TestInfo getTestInfo(String sessionId) throws JSONException{
        TestInfo ti = new TestInfo();
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(sessionId)) {
            ti.setStatus(TestStatus.NotFound.toString());
            return ti;
        }
        String url = this.urlManager.testStatus(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
        if (result.get(JsonConstants.DATA_URL) == null) {
            ti.setStatus(TestStatus.NotFound.toString());
        } else {
            ti.setId(String.valueOf(result.getInt("testId")));
            ti.setName(result.getString(JsonConstants.NAME));
            if (result.getString(JsonConstants.STATUS).equals("DATA_RECIEVED")) {
                ti.setStatus(TestStatus.Running.toString());
            } else if (result.getString(JsonConstants.STATUS).equals("ENDED")) {
                ti.setStatus(TestStatus.NotRunning.toString());
            } else {
                ti.setStatus(TestStatus.NotRunning.toString());
            }
        }
        return ti;
    }


    @Override
    public synchronized String startTest(String testId) {
        String session=null;
        if (StringUtils.isBlank(userKey)&StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        JSONObject json=this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        session = json.getJSONObject(JsonConstants.RESULT).getJSONArray("sessionsId").getString(0);
        return session;
    }

    @Override
    public int getTestCount() throws JSONException, IOException {
        if (StringUtils.isBlank(userKey)) {
            logger.println("getTests userKey is empty");
            return 0;
        }

        String url = this.urlManager.getTests(APP_KEY, userKey);

        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null,Method.GET);
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
        JSONArray stopArray=this.bzmHttpClient.getResponseAsJson(url, null,Method.GET).getJSONArray(JsonConstants.RESULT);
        String command=((JSONObject)stopArray.get(0)).getString(JsonConstants.RESULT);
        return command.equals("shutdown command sent\n");
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
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null,Method.GET);
            try {
                JSONArray arr = (JSONArray) jo.get(JsonConstants.RESULT);
                if (arr.length() > 0) {
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
                                id = String.valueOf(en.getInt(JsonConstants.ID));
                                name = en.getString(JsonConstants.NAME).replaceAll("&", "&amp;");
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
    public JSONObject getTresholds(String sessionId) {
        if (userKey == null || userKey.trim().isEmpty()) {
            return null;
        }
        String url = this.urlManager.getTresholds(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        return jo;
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
    public String getUserKey() {
        return this.userKey;
    }

    @Override
    public void setUserKey(String userKey) {
        this.userKey=userKey;
    }

    @Override
    public JSONObject putTestInfo(String testId, JSONObject data) {
        if(org.apache.commons.lang.StringUtils.isBlank(this.userKey)& org.apache.commons.lang.StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, this.userKey, testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, data, Method.PUT);
        return jo;
    }

    @Override
    public JSONObject getTestConfig(String testId){
        if(org.apache.commons.lang.StringUtils.isBlank(this.userKey)& org.apache.commons.lang.StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.getTestConfig(APP_KEY, this.userKey, testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
        return jo;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        if(org.apache.commons.lang.StringUtils.isBlank(this.userKey)& org.apache.commons.lang.StringUtils.isBlank(testId)) return null;

        String url = this.urlManager.testTerminate(APP_KEY, this.userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);

    }


    @Override
    public int getTestSessionStatusCode(String id) {
        int statusCode=0;
        if(org.apache.commons.lang.StringUtils.isBlank(this.userKey)& org.apache.commons.lang.StringUtils.isBlank(id))
        {
            return statusCode;
        }
        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, this.userKey, id);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, Method.GET);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode=result.getInt("statusCode");
        } catch (Exception e) {
        }finally {
            {
                return statusCode;
            }
        }
    }
}

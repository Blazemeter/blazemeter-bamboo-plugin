package com.blazemeter.bamboo.plugin.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.ApiVersion;
import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;
import com.blazemeter.bamboo.plugin.configuration.JsonNodes;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import com.blazemeter.bamboo.plugin.testresult.TestResultFactory;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Marcel Milea
 *
 */
public class BzmServiceManager {

    private static BzmServiceManager bzmServiceManager=null;


    private String userKey;
    private String apiVersion;
	private BlazemeterApi blazemeterApi;
	
	//Default properties

	private StringBuilder session=new StringBuilder();
	private String testId=new String();
	private JSONObject aggregate;
	
	private BzmServiceManager(){
	}

    private BzmServiceManager(String proxyserver,
                              String proxyport,
                              String proxyuser,
                              String proxypass,
                              String api_version) {
        int proxyPort= (StringUtils.isBlank(proxyport)?0:
                Integer.parseInt(proxyport));
        this.apiVersion=api_version;
        blazemeterApi = APIFactory.getAPI(proxyserver,proxyPort,proxyuser,proxypass,api_version);
    }


	private BzmServiceManager(Map<String, Object> context) {
        int proxyPort= (StringUtils.isBlank((String)context.get(BlazeMeterConstants.PROXY_SERVER_PORT))?0:
                Integer.parseInt((String)context.get(BlazeMeterConstants.PROXY_SERVER_PORT)));

        blazemeterApi = APIFactory.getAPI((String)context.get(BlazeMeterConstants.PROXY_SERVER_NAME),
                proxyPort,
                (String)context.get(BlazeMeterConstants.PROXY_USERNAME),
                (String)context.get(BlazeMeterConstants.PROXY_PASSWORD),
                (String)context.get(BlazeMeterConstants.SETTINGS_API_VERSION));
	}

    public static BzmServiceManager getBzmServiceManager(){
        if(bzmServiceManager==null){
            bzmServiceManager=new BzmServiceManager("","","","","v3");
        }
        return bzmServiceManager;
    }
    public static BzmServiceManager getBzmServiceManager(String proxyserver,
                                                         String proxyport,
                                                         String proxyuser,
                                                         String proxypass,
                                                         String api_version
    ) {
        int proxyPort= (StringUtils.isBlank(proxyport)?0:Integer.parseInt(proxyport));
        if(bzmServiceManager==null){
            bzmServiceManager=new BzmServiceManager(proxyserver,
                    proxyport,
                    proxyuser,
                    proxypass,
                    api_version);
        }
        bzmServiceManager.blazemeterApi = APIFactory.getAPI(proxyserver,
                proxyPort,proxyuser,proxypass,api_version);
    return bzmServiceManager;
    }

        public static BzmServiceManager getBzmServiceManager(Map<String, Object> context) {
        if(bzmServiceManager==null){
            bzmServiceManager=new BzmServiceManager(context);
        }else{
            bzmServiceManager.setUserKey((String)context.get(BlazeMeterConstants.USER_KEY));
            int proxyPort= (StringUtils.isBlank((String)context.get(BlazeMeterConstants.PROXY_SERVER_PORT))?0:
                    Integer.parseInt((String)context.get(BlazeMeterConstants.PROXY_SERVER_PORT)));

            bzmServiceManager.blazemeterApi = APIFactory.getAPI((String)context.get(BlazeMeterConstants.PROXY_SERVER_NAME),
                    proxyPort,
                    (String)context.get(BlazeMeterConstants.PROXY_USERNAME),
                    (String)context.get(BlazeMeterConstants.PROXY_PASSWORD),
                    (String)context.get(BlazeMeterConstants.SETTINGS_API_VERSION));
        }
        return bzmServiceManager;
    }


    @NotNull
	public String getDebugKey() {
		return "Debug Key";
	}
	
	/**
	 * returns a hash map with test id as key and test name as value
	 * @return
	 */
	public HashMap<String, String> getTests() {
		try {
			return this.blazemeterApi.getTestList(userKey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		HashMap<String, String> temp = new HashMap<String, String>();
		return temp;//no tests found, return an empty hashmap
	}

	private void addError(String error, BuildLogger logger, CurrentBuildResult currentBuildResult){
		logger.addErrorLogEntry(error);
		currentBuildResult.addBuildErrors(Arrays.asList(error));
	}
	
	public boolean startTest(String testId, BuildLogger logger, CurrentBuildResult currentBuildResult) {
        int countStartRequests = 0;
        try {
            logger.addBuildLogEntry("Trying to start test with testId="+testId+" for userKey="+userKey);
            do {
                this.session.append(this.blazemeterApi.startTest(userKey, testId));
                countStartRequests++;
                if (countStartRequests > 5) {
                    addError("Could not start BlazeMeter Test", logger, currentBuildResult);
                    return false;
                }
            } while (session.length()==0);
            logger.addBuildLogEntry("Test with testId="+testId+" was started with session="+session.toString());
        } catch (JSONException e) {
            addError("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
        }
        return true;
    }


	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
    public TestResult getReport(BuildLogger logger) {
        TestResultFactory testResultFactory = TestResultFactory.getTestResultFactory();
        testResultFactory.setVersion(ApiVersion.valueOf(apiVersion));
        TestResult testResult = null;
        try {
            logger.addBuildLogEntry("Trying to request aggregate report. UserKey="+userKey+" session="+this.session.toString());
            this.aggregate=this.blazemeterApi.testReport(this.userKey,this.session.toString());
            testResult = testResultFactory.getTestResult(this.aggregate);
            logger.addBuildLogEntry(testResult.toString());
        } catch (JSONException e) {
            logger.addErrorLogEntry("Problems with getting aggregate test report...",e);
        } catch (IOException e) {
            logger.addErrorLogEntry("Problems with getting aggregate test report...", e);
        } catch (NullPointerException e){
            logger.addErrorLogEntry("Problems with getting aggregate test report...", e);
        }
        finally {
            return testResult;
        }
    }
	public boolean uploadJMX(String testId, String filename, String pathname){
		return this.blazemeterApi.uploadJmx(userKey, testId, filename, pathname);
	}
	
    public void uploadFile(String testId, String dataFolder, String fileName, BuildLogger logger, CurrentBuildResult currentBuildResult) {
        JSONObject json = this.blazemeterApi.uploadFile(userKey, testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get(JsonNodes.RESPONSE_CODE).equals(new Integer(200))) {
            	addError("Could not upload file " + fileName + " " + json.get("error").toString(), logger, currentBuildResult);
            }
        } catch (JSONException e) {
        	addError("Could not upload file " + fileName + " " + e.getMessage(), logger, currentBuildResult);
        }
    } 	

    public void stopTest(String testId, BuildLogger logger, CurrentBuildResult currentBuildResult){
         boolean stopTest=true;
    	int countStartRequests = 0;
        logger.addBuildLogEntry("Trying to stop test with testId="+testId+" for session="+this.session.toString()+" for userKey="+userKey);
        try {
            do {
        	stopTest = this.blazemeterApi.stopTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
            	addError("Could not stop BlazeMeter Test "+ testId, logger, currentBuildResult);
            	return;
            }
        } while (stopTest == false);
        

			if (stopTest==true) {
				logger.addBuildLogEntry("Test stopped succesfully. testId="+testId+" userKey="+this.userKey+" session="+this.session.toString());
			} else {
				addError("Error stopping test: ",logger, currentBuildResult);
				addError("Please use BlazeMeter website to manually stop the test with ID: " + testId, logger, currentBuildResult);
			}
		} catch (JSONException e) {
			addError("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
			addError("Please use BlazeMeter website to manually stop the test with ID: " + testId, logger, currentBuildResult);
		}
    	
    }
    
    public TestInfo getTestStatus(){
        if(blazemeterApi instanceof BlazemeterApiV2Impl){
            return this.blazemeterApi.getTestRunStatus(userKey, testId);
        }else{
            return this.blazemeterApi.getTestRunStatus(userKey, session.toString());
        }
    }
    
	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public StringBuilder getSession() {
		return session;
	}


	public boolean verifyUserKey(String userKey){
		return this.blazemeterApi.verifyUserKey(userKey);
	}

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }
}

package com.blazemeter.bamboo.plugin.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.ApiVersion;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonNodes;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import com.blazemeter.bamboo.plugin.testresult.TestResultFactory;
import com.google.common.collect.LinkedHashMultimap;
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
        int proxyPort= (StringUtils.isBlank((String)context.get(AdminServletConst.PROXY_SERVER_PORT))?0:
                Integer.parseInt((String)context.get(AdminServletConst.PROXY_SERVER_PORT)));

        blazemeterApi = APIFactory.getAPI((String)context.get(AdminServletConst.PROXY_SERVER_NAME),
                proxyPort,
                (String)context.get(AdminServletConst.PROXY_USERNAME),
                (String)context.get(AdminServletConst.PROXY_PASSWORD),
                (String)context.get(Constants.SETTINGS_API_VERSION));
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
            bzmServiceManager.setUserKey((String)context.get(Constants.USER_KEY));
            int proxyPort= (StringUtils.isBlank((String)context.get(AdminServletConst.PROXY_SERVER_PORT))?0:
                    Integer.parseInt((String)context.get(AdminServletConst.PROXY_SERVER_PORT)));

            bzmServiceManager.blazemeterApi = APIFactory.getAPI((String)context.get(AdminServletConst.PROXY_SERVER_NAME),
                    proxyPort,
                    (String)context.get(AdminServletConst.PROXY_USERNAME),
                    (String)context.get(AdminServletConst.PROXY_PASSWORD),
                    (String)context.get(Constants.SETTINGS_API_VERSION));
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
	public LinkedHashMultimap<String, String> getTests() {
        LinkedHashMultimap<String,String> tests= LinkedHashMultimap.create();
        try {
			tests=this.blazemeterApi.getTestList(userKey);
		} catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return tests;
        }
    }

    public Map<String, Collection<String>> getTestsAsMap() {
        return this.getTests().asMap();
    }

	public boolean startTest(String testId, BuildLogger logger) {
        int countStartRequests = 0;
        try {
            logger.addBuildLogEntry("Trying to start test with testId="+testId+" for userKey="+userKey);
            do {
                this.session.append(this.blazemeterApi.startTest(userKey, testId));
                countStartRequests++;
                if (countStartRequests > 5) {
                    logger.addErrorLogEntry("Could not start BlazeMeter Test with userKey=" + userKey + " testId=" + testId);
                    return false;
                }
            } while (session.length()==0);
            logger.addBuildLogEntry("Test with testId="+testId+" was started with session="+session.toString());
        } catch (JSONException e) {
            logger.addErrorLogEntry("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
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
	
    public void uploadFile(String testId, String dataFolder, String fileName, BuildLogger logger) {
        JSONObject json = this.blazemeterApi.uploadFile(userKey, testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get(JsonNodes.RESPONSE_CODE).equals(new Integer(200))) {
                logger.addErrorLogEntry("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.addErrorLogEntry("Could not upload file " + e.getMessage());
        }
    } 	

    public void stopTest(String testId, BuildLogger logger){
         boolean stopTest=true;
    	int countStartRequests = 0;
        logger.addBuildLogEntry("Trying to stop test with testId="+testId+" for session="+this.session.toString()+" for userKey="+userKey);
        try {
            do {
        	stopTest = this.blazemeterApi.stopTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
                logger.addErrorLogEntry("Could not stop BlazeMeter test with testId=" + testId + " userKey=" + userKey);
            	return;
            }
        } while (stopTest == false);


			if (stopTest==true) {
				logger.addBuildLogEntry("Test stopped succesfully. testId="+testId+" userKey="+this.userKey+" session="+this.session.toString());
			} else {
                logger.addErrorLogEntry("Error while stopping test with testId=" + testId + " userKey=" + this.userKey + " session=" + this.session.toString());
			}
		} catch (JSONException e) {
            logger.addErrorLogEntry("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]");
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

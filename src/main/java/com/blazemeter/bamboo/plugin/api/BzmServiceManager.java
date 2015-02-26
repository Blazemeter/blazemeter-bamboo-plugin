package com.blazemeter.bamboo.plugin.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;
import com.blazemeter.bamboo.plugin.configuration.JSON_NODES;
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

	private String session;
	private String aggregate;
	
	private BzmServiceManager(){
	}

    private BzmServiceManager(String proxyserver,
                              String proxyport,
                              String proxyuser,
                              String proxypass,
                              String api_version) {
        int proxyPort= (StringUtils.isBlank(proxyport)?0:
                Integer.parseInt(proxyport));

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
        JSONObject json;
        int countStartRequests = 0;
        do {
            json = this.blazemeterApi.startTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
            	addError("Could not start BlazeMeter Test", logger, currentBuildResult);
                return false;
            }
        } while (json == null);
        
        try {
			if (!json.get(JSON_NODES.RESPONSE_CODE).equals(200)) {
				if (json.get(JSON_NODES.RESPONSE_CODE).equals(500) && json.get("error").toString().startsWith("Test already running")) {
					addError("Test already running, please stop it first", logger, currentBuildResult);
					return false;
				}
                //Try again.
				json = this.blazemeterApi.startTest(userKey, testId);
                if (!json.get(JSON_NODES.RESPONSE_CODE).equals(200)) {
                	addError("Could not start BlazeMeter Test -" + json.get("error").toString(), logger, currentBuildResult);
                    return false;
                } 				
			}
			session = json.get("session_id").toString();
		} catch (JSONException e) {
			addError("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
		}
		return true;
	}

	public boolean isReportReady(){
        //get testGetArchive information
		JSONObject json = this.blazemeterApi.aggregateReport(userKey, session);
        try {
            if (json.get(JSON_NODES.RESPONSE_CODE).equals(404))
                return false;
            else
            	if (json.get(JSON_NODES.RESPONSE_CODE).equals(200)){
            		return true;
            	}
        } catch (JSONException e) {
        } 
        return false;
    }
	
	@SuppressWarnings("static-access")
	public boolean waitForReport(BuildLogger logger, CurrentBuildResult currentBuildResult){
        //get testGetArchive information
		JSONObject json = this.blazemeterApi.aggregateReport(userKey, session);
        for (int i = 0; i < 200; i++) {
            try {
                if (json.get(JSON_NODES.RESPONSE_CODE).equals(404))
                    json = this.blazemeterApi.aggregateReport(userKey, session);
                else
                    break;
            } catch (JSONException e) {
            } finally {
                try {
					Thread.currentThread().sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
        
        aggregate = null;

        for (int i = 0; i < 30; i++) {
            try {
                if (!json.get(JSON_NODES.RESPONSE_CODE).equals(200)){
                	addError("Error: Requesting aggregate report response code:" + json.get(JSON_NODES.RESPONSE_CODE), logger, currentBuildResult);
                }
                aggregate = json.getJSONObject("report").get("aggregate").toString();
            } catch (JSONException e) {
            	addError("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
            }

            if (!aggregate.equals("null"))
                break;

            try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            json = this.blazemeterApi.aggregateReport(userKey, session);
        }

        if (aggregate == null) {
        	addError("Error: Requesting aggregate is not available", logger, currentBuildResult);
            return false;
        }
     
        return true;
    }
	
	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
	public int getReport(int errorFailedThreshold, int errorUnstableThreshold, int responseTimeFailedThreshold, int responseTimeUnstableThreshold, BuildLogger logger, CurrentBuildResult currentBuildResult){
        AggregateTestResult aggregateTestResult;
		try {
			aggregateTestResult = AggregateTestResult.generate(aggregate);

		} catch (IOException e) {
			addError("Error: Requesting aggregate Test Result is not available", logger, currentBuildResult);
			return -1;
		}
		
        if (aggregateTestResult == null) {
        	addError("Error: Requesting aggregate Test Result is not available", logger, currentBuildResult);
            return -1;
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = aggregateTestResult.getErrorPercentage();
        double AverageResponseTime = aggregateTestResult.getAverage();

        if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
        	addError("Test ended with failure on error percentage threshold", logger, currentBuildResult);
            return -1;
        } else if (errorUnstableThreshold >= 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
        	addError("Test ended with unstable on error percentage threshold", logger, currentBuildResult);
            return 1;
        }

        if (responseTimeFailedThreshold >= 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
        	addError("Test ended with failure on response time threshold", logger, currentBuildResult);
            return -1;
        } else if (responseTimeUnstableThreshold >= 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
        	addError("Test ended with unstable on response time threshold", logger, currentBuildResult);
            return 1;
        }

        return 0;   		
	}

	public boolean uploadJMX(String testId, String filename, String pathname){
		return this.blazemeterApi.uploadJmx(userKey, testId, filename, pathname);
	}
	
    public void uploadFile(String testId, String dataFolder, String fileName, BuildLogger logger, CurrentBuildResult currentBuildResult) {
        JSONObject json = this.blazemeterApi.uploadFile(userKey, testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get(JSON_NODES.RESPONSE_CODE).equals(new Integer(200))) {
            	addError("Could not upload file " + fileName + " " + json.get("error").toString(), logger, currentBuildResult);
            }
        } catch (JSONException e) {
        	addError("Could not upload file " + fileName + " " + e.getMessage(), logger, currentBuildResult);
        }
    } 	

    public void stopTest(String testId, BuildLogger logger, CurrentBuildResult currentBuildResult){
    	JSONObject json;

    	int countStartRequests = 0;
        do {
        	json = this.blazemeterApi.stopTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
            	addError("Could not stop BlazeMeter Test "+ testId, logger, currentBuildResult);
            	return ;
            }
        } while (json == null);
        
        try {
			if (json.get(JSON_NODES.RESPONSE_CODE).equals(200)) {
				logger.addBuildLogEntry("Test stopped succesfully.");
			} else {
				String error = json.get("error").toString();
				addError("Error stopping test. Reported error is: "+error, logger, currentBuildResult);
				addError("Please use BlazeMeter website to manually stop the test with ID: " + testId, logger, currentBuildResult);
			}
		} catch (JSONException e) {
			addError("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]", logger, currentBuildResult);
			addError("Please use BlazeMeter website to manually stop the test with ID: " + testId, logger, currentBuildResult);
		}
    	
    }
    
    public TestInfo getTestStatus(String testId){
    	return this.blazemeterApi.getTestRunStatus(userKey, testId);
    }
    
	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getSession() {
		return session;
	}

	public boolean publishReportArtifact(CurrentBuildResult currentBuildResult) {
        AggregateTestResult aggregateTestResult;
		try {
			aggregateTestResult = AggregateTestResult.generate(aggregate);
			if (aggregateTestResult == null){
				return false;
			} 
			//TODO
	        double errorPercent = aggregateTestResult.getErrorPercentage();
	        double AverageResponseTime = aggregateTestResult.getAverage();

	        currentBuildResult.getCustomBuildData().put(BlazeMeterConstants.REPORT_RESPONSE_TIME, ""+AverageResponseTime);
	        currentBuildResult.getCustomBuildData().put(BlazeMeterConstants.REPORT_ERROR_THRESHOLD, ""+errorPercent);
	        return true;
		} catch (IOException e) {
//			logger.error("Error: Requesting aggregate Test Result is not available");
		}
		return false;
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


}

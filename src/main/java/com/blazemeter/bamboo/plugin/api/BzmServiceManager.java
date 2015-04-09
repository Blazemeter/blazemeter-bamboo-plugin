package com.blazemeter.bamboo.plugin.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonNodes;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Marcel Milea
 *
 */
public class BzmServiceManager {
private BzmServiceManager(){
	}

    @NotNull
	public String getDebugKey() {
		return "Debug Key";
	}
	
	/**
	 * returns a hash map with test id as key and test name as value
	 * @return
	 */
	public static LinkedHashMultimap<String, String> getTests(BlazemeterApi api) {
        LinkedHashMultimap<String,String> tests= LinkedHashMultimap.create();
        try {
			tests=api.getTestList();
		} catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return tests;
        }
    }

    public static Map<String, Collection<String>> getTestsAsMap(BlazemeterApi api) {
        return getTests(api).asMap();
    }

	public static String startTest(BlazemeterApi api,String testId, BuildLogger logger) {
        int countStartRequests = 0;
        String session=null;
        try {
            logger.addBuildLogEntry("Trying to start test with testId="+testId+" for userKey="+api.getUserKey());
            do {
                session=api.startTest(testId);
                countStartRequests++;
                if (countStartRequests > 5) {
                    logger.addErrorLogEntry("Could not start BlazeMeter Test with userKey=" + api.getUserKey() + " testId=" + testId);
                    return session;
                }
            } while (session.length()==0);
            logger.addBuildLogEntry("Test with testId="+testId+" was started with session="+session.toString());
        } catch (JSONException e) {
            logger.addErrorLogEntry("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
        }
        return session;
    }


	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
    public static TestResult getReport(BlazemeterApi api,String session, BuildLogger logger) {
        TestResult testResult = null;
        try {
            logger.addBuildLogEntry("Trying to request aggregate report. UserKey="+api.getUserKey()+" session="+session);
            JSONObject aggregate=api.testReport(session);
            testResult = new TestResult(aggregate);
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
	public boolean uploadJMX(BlazemeterApi api,String testId, String filename, String pathname){
		return api.uploadJmx(testId, filename, pathname);
	}
	
    public void uploadFile(BlazemeterApi api,String testId, String dataFolder, String fileName, BuildLogger logger) {
        JSONObject json = api.uploadFile(testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get(JsonNodes.RESPONSE_CODE).equals(new Integer(200))) {
                logger.addErrorLogEntry("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.addErrorLogEntry("Could not upload file " + e.getMessage());
        }
    } 	

    public static void stopTest(BlazemeterApi api,String testId, String session, BuildLogger logger){
        boolean stopTest=true;
        logger.addBuildLogEntry("Trying to stop test with testId="+testId+" for session="+session+" for userKey="+api.getUserKey());
        stopTest = api.stopTest(testId);
        try {
			if (stopTest==true) {
				logger.addBuildLogEntry("Test stopped succesfully. testId="+testId+" userKey="+api.getUserKey()+" session="+session);
			} else {
                logger.addErrorLogEntry("Error while stopping test with testId=" + testId + " userKey=" + api.getUserKey() + " session=" + session);
			}
		} catch (JSONException e) {
            logger.addErrorLogEntry("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]");
		}
    }
    
    public static TestInfo getTestStatus(BlazemeterApi api,String testId,String session){
        if(api instanceof BlazemeterApiV2Impl){
            return api.getTestRunStatus(testId);
        }else{
            return api.getTestRunStatus(session);
        }
    }


    public static boolean validateServerTresholds(BlazemeterApi api,String session,BuildLogger logger) {
        JSONObject jo = null;
        boolean tresholdsValid=true;
        JSONObject result=null;
        logger.addBuildLogEntry("Going to validate server tresholds...");
        try {
            jo=api.getTresholds(session);
            result=jo.getJSONObject(JsonNodes.RESULT);
            tresholdsValid=result.getJSONObject(JsonNodes.DATA).getBoolean("success");
        } catch (NullPointerException e){
            logger.addErrorLogEntry("Server tresholds validation was not executed");
            logger.addErrorLogEntry(e.getMessage());
        }catch (JSONException je) {
            logger.addErrorLogEntry("Server tresholds validation was not executed");
            logger.addErrorLogEntry("Failed to get tresholds for  session=" + session);
        }finally {
            logger.addBuildLogEntry("Server tresholds validation " +
                    (tresholdsValid ? "passed. Marking build as PASSED" : "failed. Marking build as FAILED"));
            return tresholdsValid;
        }
    }
}

package com.blazemeter.bamboo.plugin;

import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.CIStatus;
import com.blazemeter.bamboo.plugin.api.TestType;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Zmicer Kashlach
 *
 */
public class ServiceManager {
private ServiceManager(){
	}

    public static String getReportUrl(Api api, String masterId, BuildLogger logger) {
        JSONObject jo=null;
        String publicToken="";
        String reportUrl=null;
        try {
            jo = api.publicToken(masterId);
            if(jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject(JsonConstants.RESULT);
                publicToken=result.getString("publicToken");
                reportUrl=api.url()+"/app/?public-token="+publicToken+"#masters/"+masterId+"/summary";
            }else{
                logger.addErrorLogEntry("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                logger.addErrorLogEntry("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                reportUrl=api.url()+"/app/#masters/"+masterId+"/summary";
            }

        } catch (Exception e){
            logger.addErrorLogEntry("Problems with generating public-token for report URL");
            logger.addErrorLogEntry("Problems with generating public-token for report URL",e);
        }finally {
            return reportUrl;
        }
    }

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(ServiceManager.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty("version", "N/A");
        }
        return props.getProperty("version");
    }

    @NotNull
	public String getDebugKey() {
		return "Debug Key";
	}
	
	/**
	 * returns a hash map with test id as key and test name as value
	 * @return
	 */
	public static LinkedHashMultimap<String, String> getTests(Api api) {
        LinkedHashMultimap<String,String> tests= LinkedHashMultimap.create();
        try {
			tests=api.getTestList();
		} catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            tests.put("Check blazemeter & proxy-settings",
                      "Check blazemeter & proxy-settings");
        }finally {
            return tests;
        }
    }

    public static Map<String, Collection<String>> getTestsAsMap(Api api) {
        return getTests(api).asMap();
    }

	public static String startTest(Api api, String testId, BuildLogger logger) {
        int countStartRequests = 0;
        String masterId=null;
        try {
            logger.addBuildLogEntry("Trying to start test with testId="+testId+" for userKey="+api.getUserKey());
            TestType testType=getTestType(api,testId,logger);
            do {
                masterId=api.startTest(testId,testType);
                countStartRequests++;
                if (countStartRequests > 5) {
                    logger.addErrorLogEntry("Could not start BlazeMeter Test with userKey=" + api.getUserKey() + " testId=" + testId);
                    return masterId;
                }
            } while (masterId.length()==0);
            logger.addBuildLogEntry("Test with testId="+testId+" was started with masterId="+masterId.toString());
        } catch (Exception e) {
            logger.addErrorLogEntry("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            logger.addErrorLogEntry("Check server & proxy settings");
        }
        return masterId;
    }


	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
    public static TestResult getReport(Api api, String masterId, BuildLogger logger) {
        TestResult testResult = null;
        try {
            logger.addBuildLogEntry("Trying to request aggregate report. UserKey="+api.getUserKey()+" masterId="+masterId);
            JSONObject aggregate=api.testReport(masterId);
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

    public static TaskState ciStatus(Api api, String masterId, BuildLogger logger) {
        JSONObject jo;
        JSONArray failures=new JSONArray();
        JSONArray errors=new JSONArray();
        try {
            jo=api.ciStatus(masterId);
            logger.addBuildLogEntry("Test status object = " + jo.toString());
            JSONObject result=jo.getJSONObject(JsonConstants.RESULT);
            failures=result.getJSONArray(JsonConstants.FAILURES);
            errors=result.getJSONArray(JsonConstants.ERRORS);
        } catch (JSONException je) {
            logger.addErrorLogEntry("No thresholds on server: setting 'success' for CIStatus ");
        } catch (Exception e) {
            logger.addErrorLogEntry("No thresholds on server: setting 'success' for CIStatus ");
        }finally {
            if(errors.length()>0){
                logger.addErrorLogEntry("Having errors while test status validation...");
                logger.addErrorLogEntry("Errors: " + errors.toString());
                logger.addErrorLogEntry("Setting CIStatus="+ CIStatus.errors.name());
                return TaskState.ERROR;
            }
            if(failures.length()>0){
                logger.addErrorLogEntry("Having failures while test status validation...");
                logger.addErrorLogEntry("Failures: " + failures.toString());
                logger.addErrorLogEntry("Setting CIStatus="+CIStatus.failures.name());
                return TaskState.FAILED;
            }
            logger.addBuildLogEntry("No errors/failures while validating CIStatus: setting "+CIStatus.success.name());
        }
        return TaskState.SUCCESS;
    }


    public static boolean stopTestMaster(Api api, String masterId, BuildLogger logger) {
        boolean terminate=false;
        try {

            int statusCode = api.masterStatus(masterId);
            if (statusCode < 100) {
                api.terminateTest(masterId);
                terminate=true;
            }
            if (statusCode >= 100|statusCode ==-1) {
                api.stopTest(masterId);
                terminate=false;
            }
        } catch (Exception e) {
            logger.addBuildLogEntry("Error while trying to stop test with testId=" + masterId + ", " + e.getMessage());
        }finally {
            return terminate;
        }
    }

    private static TestType getTestType(Api api,String testId,BuildLogger logger){
        TestType testType=TestType.http;
        logger.addBuildLogEntry("Detecting testType....");
        try{
            JSONArray result=api.getTestsJSON().getJSONArray(JsonConstants.RESULT);
            int resultLength=result.length();
            for (int i=0;i<resultLength;i++){
                JSONObject jo=result.getJSONObject(i);
                if(String.valueOf(jo.getInt(JsonConstants.ID)).equals(testId)){
                    testType= TestType.valueOf(jo.getString(JsonConstants.TYPE));
                    logger.addBuildLogEntry("Received testType=" + testType.toString() + " for testId=" + testId);
                }
            }
        } catch (Exception e) {
            logger.addBuildLogEntry("Error while detecting type of test:" + e);
        }finally {
            return testType;
        }
    }


    public static void downloadJtlReports(Api api,String masterId, TaskContext context, BuildLogger logger) {
        List<String> sessionsIds = api.getListOfSessionIds(masterId);
        File jtlDir=new File(context.getWorkingDirectory().getAbsolutePath()+"/"+context.getBuildContext().getBuildResultKey());
        for (String s : sessionsIds) {
//            downloadJtlReport(api, s, filePath,buildNumber, jenBuildLog, bzmBuildLog);
        }
    }


}

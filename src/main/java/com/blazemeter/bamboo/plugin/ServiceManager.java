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

package com.blazemeter.bamboo.plugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.util.concurrent.NotNull;
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.CIStatus;
import com.blazemeter.bamboo.plugin.api.TestType;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceManager {
    private final static int BUFFER_SIZE = 2048;
    private final static int DELAY=10000;

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


    public static boolean notes(Api api, String masterId, String notes,BuildLogger logger){
        boolean note=false;
        int n = 1;
        logger.addBuildLogEntry("Trying to PATCH notes to test report on server: masterId=" + masterId);
        while (!note && n < 6) {
            try {
                Thread.sleep(DELAY);
                int statusCode = api.masterStatus(masterId);
                if (statusCode > 20) {
                    note = api.notes(notes, masterId);
                }
            } catch (Exception e) {
                logger.addErrorLogEntry("Failed to PATCH notes to test report on server: masterId=" + masterId + " " + e.getMessage());
            } finally {
                n++;
            }

        }
        return note;
    }

    public static Map<String, Collection<String>> getTestsAsMap(Api api) {
        return getTests(api).asMap();
    }

	public static String startTest(Api api, String testId, BuildLogger logger) {
        int countStartRequests = 0;
        String startResponse=null;
        try {
            logger.addBuildLogEntry("Trying to start test with testId="+testId+" for userKey="+api.getUserKey());
            TestType testType=getTestType(api,testId,logger);
            do {
                startResponse=api.startTest(testId,testType);
                countStartRequests++;
                if (countStartRequests > 5) {
                    logger.addErrorLogEntry("Could not start BlazeMeter Test with userKey=" + api.getUserKey() + " testId=" + testId);
                    return startResponse;
                }
            } while (startResponse.length()==0);
            Integer.parseInt(startResponse);
            logger.addBuildLogEntry("Test with testId="+testId+" was started with masterId="+startResponse);
        }catch (NumberFormatException e) {
            logger.addErrorLogEntry("Error while starting BlazeMeter Test: "+startResponse);
            throw new NumberFormatException(startResponse);
        }catch (Exception e) {
            logger.addErrorLogEntry("Error while starting BlazeMeter Test [" + e.getMessage() + "]");
            logger.addErrorLogEntry("Check server & proxy settings");
        }
        return startResponse;
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
            logger.addErrorLogEntry("Problems with getting aggregate test report - check test report on server");
        } catch (IOException e) {
            logger.addErrorLogEntry("Problems with getting aggregate test report - check test report on server");
        } catch (NullPointerException e){
            logger.addErrorLogEntry("Problems with getting aggregate test report - check test report on server");
        }
        finally {
            return testResult;
        }
    }

    public static TaskState ciStatus(Api api, String masterId, BuildLogger logger) {
        JSONObject jo;
        TaskState taskState = TaskState.SUCCESS;
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
            if (errors.length() > 0) {
                logger.addErrorLogEntry("Having errors while test status validation...");
                logger.addErrorLogEntry("Errors: " + errors.toString());
                logger.addErrorLogEntry("Setting CIStatus=" + CIStatus.errors.name());
                taskState = errorsFailed(errors) ? TaskState.FAILED : TaskState.ERROR;
            }
            if (failures.length() > 0) {
                logger.addErrorLogEntry("Having failures while test status validation...");
                logger.addErrorLogEntry("Failures: " + failures.toString());
                logger.addErrorLogEntry("Setting CIStatus=" + CIStatus.failures.name());
                taskState=TaskState.FAILED;
                return taskState;
            }
            if (taskState.equals(TaskState.SUCCESS)) {
                logger.addBuildLogEntry("No errors/failures while validating CIStatus: setting " + CIStatus.success.name());
            }
        }
        return taskState;
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


    public static void downloadJtlReports(Api api,String masterId, File jtlDir, BuildLogger logger){
        List<String> sessionsIds = null;
        try {
            sessionsIds = api.getListOfSessionIds(masterId);
            for (String s : sessionsIds) {
                downloadJtlReport(api, s, jtlDir, logger);
            }
        } catch (Throwable e) {
            logger.addErrorLogEntry("Failed to download jtl reports: "+e.getMessage());
        }
    }

    public static void downloadJunitReport(Api api,String masterId, File junitD, BuildLogger logger) {
        try {
            String junit = api.retrieveJunit(masterId);
            File junitFile=new File(junitD,Constants.BM_TRESHOLDS);
            logger.addBuildLogEntry("Trying to save junit report to "+junitFile.getAbsolutePath());
            FileUtils.writeStringToFile(junitFile,junit);
        } catch (Exception e) {
            logger.addErrorLogEntry("Failed to get junit report for test with masterId="+masterId,e);
        }
    }


    public static void downloadJtlReport(Api api, String sessionId, File jtlDir,BuildLogger logger) {

        String dataUrl=null;
        URL url=null;
        try {
            JSONObject jo=api.retrieveJtlZip(sessionId);
            JSONArray data=jo.getJSONObject(JsonConstants.RESULT).getJSONArray(JsonConstants.DATA);
            for(int i=0;i<data.length();i++){
                String title=data.getJSONObject(i).getString("title");
                if(title.equals("Zip")){
                    dataUrl=data.getJSONObject(i).getString(JsonConstants.DATA_URL);
                    break;
                }
            }
            File jtlZip=new File(jtlDir + "/" +sessionId+"-"+ Constants.BM_ARTEFACTS);
            url=new URL(dataUrl);
            logger.addBuildLogEntry("Jtl url = " + url.toString() + " sessionId = " + sessionId);
            int i = 1;
            boolean jtl = false;
            while (!jtl && i < 4) {
                try {
                    logger.addBuildLogEntry("Downloading JTLZIP for sessionId = " + sessionId + " attemp # " + i);
                    int conTo = (int) (10000 * Math.pow(3, i - 1));
                    logger.addBuildLogEntry("Saving ZIP to " + jtlZip.getAbsolutePath());
                    FileUtils.copyURLToFile(url, jtlZip,conTo,30000);
                    jtl = true;
                } catch (Exception e) {
                    logger.addErrorLogEntry("Unable to get JTLZIP from " + url + ", " + e);
                } finally {
                    i++;
                }
            }
            String jtlZipCanonicalPath=jtlZip.getCanonicalPath();
            unzip(jtlZip.getAbsolutePath(), jtlZipCanonicalPath.substring(0,jtlZipCanonicalPath.length()-4), logger);
            File sample_jtl=new File(jtlDir,"sample.jtl");
            File bm_kpis_jtl=new File(jtlDir,Constants.BM_KPIS);
            if(sample_jtl.exists()){
                sample_jtl.renameTo(bm_kpis_jtl);
            }
            FileUtils.deleteQuietly(jtlZip);
        } catch (JSONException e) {
            logger.addErrorLogEntry("Unable to get  JTLZIP from "+url+" "+e.getMessage());
        } catch (MalformedURLException e) {
            logger.addErrorLogEntry("Unable to get  JTLZIP from "+url+" "+e.getMessage());
        } catch (IOException e) {
            logger.addErrorLogEntry("Unable to get JTLZIP from "+url+" "+e.getMessage());
        } catch (Exception e) {
            logger.addErrorLogEntry("Unable to get JTLZIP from "+url+" "+e.getMessage());
        }
    }


    public static JSONArray prepareSessionProperties(String sesssionProperties, BuildLogger logger) throws JSONException {
        List<String> propList = Arrays.asList(sesssionProperties.split(","));
        JSONArray props = new JSONArray();
        logger.addBuildLogEntry("Preparing jmeter properties for the test...");
        for (String s : propList) {
            try {
                JSONObject prop = new JSONObject();
                List<String> pr = Arrays.asList(s.split("="));
                if (pr.size() > 1) {
                    prop.put("key", pr.get(0).trim());
                    prop.put("value", pr.get(1).trim());
                }
                props.put(prop);
            } catch (Exception e) {
                logger.addErrorLogEntry("Failed to prepare jmeter property " + s + " for the test: " + e.getMessage());
            }
        }
        return props;
    }

    public static File resolvePath(TaskContext context, String path,BuildLogger logger) throws Exception {
        File f = null;
        File root = new File("/");
        if (path.startsWith("/")) {
            f = new File(root, path);
        } else {
            f = new File(context.getWorkingDirectory().getAbsolutePath()+"/build # "
                    +context.getBuildContext().getBuildNumber(), path);
        }
        if (!f.exists()) {
            boolean mkDir = false;
            try {
                mkDir = f.mkdirs();
            } catch (Exception e) {
                throw new Exception("Failed to find filepath = " + f.getName());
            } finally {
                if (!mkDir) {
                    logger.addBuildLogEntry("Failed to create "+f.getCanonicalPath()+" , workspace will be used.");
                    f = new File(context.getWorkingDirectory(), path);
                    f.mkdirs();
                    logger.addBuildLogEntry("Resolving path into "+f.getCanonicalPath());
                }
            }
        }
        return f.getCanonicalFile();
    }

    public static void properties(Api api, JSONArray properties, String masterId, BuildLogger logger){
        List<String> sessionsIds=null;
        try{
            sessionsIds = api.getListOfSessionIds(masterId);
        }catch (Exception e){
            logger.addErrorLogEntry("Failed to get sessionIds: "+e.getMessage());
        }
        logger.addBuildLogEntry("Trying to submit jmeter properties: got " + sessionsIds.size() + " sessions");
        for (String s : sessionsIds) {
            logger.addBuildLogEntry("Submitting jmeter properties to sessionId=" + s);
            int n = 1;
            boolean submit = false;
            while (!submit && n < 6) {
                try {
                    submit = api.properties(properties, s);
                    if (!submit) {
                        logger.addBuildLogEntry("Failed to submit jmeter properties to sessionId=" + s+" retry # "+n);
                        Thread.sleep(DELAY);
                    }
                } catch (Exception e) {
                    logger.addErrorLogEntry("Failed to submit jmeter properties to sessionId=" + s, e);
                } finally {
                    n++;
                }
            }
        }
    }


    public static void unzip(String srcZipFileName,
                             String destDirectoryName, BuildLogger logger) {
        try {
            BufferedInputStream bufIS = null;
            // create the destination directory structure (if needed)
            File destDirectory = new File(destDirectoryName);
            destDirectory.mkdirs();

            // open archive for reading
            File file = new File(srcZipFileName);
            ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ);

            //for every zip archive entry do
            Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                    logger.addBuildLogEntry("\tExtracting jtl report: " + entry);

                    //create destination file
                    File destFile = new File(destDirectory, entry.getName());

                    //create parent directories if needed
                    File parentDestFile = destFile.getParentFile();
                    parentDestFile.mkdirs();

                    bufIS = new BufferedInputStream(
                            zipFile.getInputStream(entry));
                    int currentByte;

                    // buffer for writing file
                    byte data[] = new byte[BUFFER_SIZE];

                    // write the current file to disk
                    FileOutputStream fOS = new FileOutputStream(destFile);
                    BufferedOutputStream bufOS = new BufferedOutputStream(fOS, BUFFER_SIZE);

                    while ((currentByte = bufIS.read(data, 0, BUFFER_SIZE)) != -1) {
                        bufOS.write(data, 0, currentByte);
                    }

                    // close BufferedOutputStream
                    bufOS.flush();
                    bufOS.close();
            }
            bufIS.close();
        } catch (Exception e) {
            logger.addErrorLogEntry("Failed to unzip report: check that it is downloaded");
        }
    }


    public static boolean errorsFailed(JSONArray errors) {
        int l = errors.length();
        for (int i = 0; i < l; i++) {
            try {
                if (errors.getJSONObject(i).getInt(JsonConstants.CODE) == 0 | errors.getJSONObject(i).getInt(JsonConstants.CODE) == 70404) {
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException je) {
                return false;
            }
        }
        return false;
    }

}

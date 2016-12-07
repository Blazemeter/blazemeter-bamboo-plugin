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

import java.io.File;
import java.util.HashMap;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.*;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.api.*;
import com.blazemeter.bamboo.plugin.configuration.StaticAccessor;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

public class TaskType implements com.atlassian.bamboo.task.TaskType {
	private static final int CHECK_INTERVAL = 30000;
    private static final int INIT_TEST_TIMEOUT = 600000;

    String testId;
    String jmeterProps;
    String masterId;
    String notes;
    String jtlPath;
    String junitPath;
    Api api;
    boolean jtlReport=false;
    boolean junitReport=false;
	File rootDirectory;

	ProcessService processService;
	private final PluginSettingsFactory pluginSettingsFactory;

	public TaskType(final ProcessService processService, PluginSettingsFactory pluginSettingsFactory){
		this.processService = processService;
		this.pluginSettingsFactory = pluginSettingsFactory;
	}

 	@Override
	public TaskResult execute(TaskContext context) throws TaskException {
        final BuildLogger logger = context.getBuildLogger();
        TaskResultBuilder resultBuilder = TaskResultBuilder.create(context);
        ConfigurationMap configMap = context.getConfigurationMap();
        logger.addBuildLogEntry("Executing BlazeMeter task...");
        logger.addBuildLogEntry("BlazemeterBamboo plugin v."+ ServiceManager.getVersion());
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String userKey = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
        String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
        String testId=configMap.get(Constants.SETTINGS_SELECTED_TEST_ID);
        int point=testId.indexOf(".");
        this.testId = testId.substring(0,point);
        this.jmeterProps = configMap.get(Constants.SETTINGS_JMETER_PROPERTIES);
        this.jtlReport=configMap.getAsBoolean(Constants.SETTINGS_JTL_REPORT);
        this.junitReport=configMap.getAsBoolean(Constants.SETTINGS_JUNIT_REPORT);
        this.notes = configMap.get(Constants.SETTINGS_NOTES);
        this.jtlPath=configMap.get(Constants.SETTINGS_JTL_PATH);
        this.junitPath=configMap.get(Constants.SETTINGS_JUNIT_PATH);
        if (StringUtils.isBlank(userKey)) {
            logger.addErrorLogEntry("BlazeMeter user key not defined!");
            return resultBuilder.failed().build();
        }
        File dd=new File(context.getWorkingDirectory().getAbsolutePath()+"/build # "
                +context.getBuildContext().getBuildNumber());
        String httpLog=dd+File.separator+Constants.HTTP_LOG;
        this.api = new ApiV3Impl(userKey, serverUrl);

        rootDirectory = context.getRootDirectory();
        logger.addBuildLogEntry("Attempting to start test with id:" + testId);
        logger.addBuildLogEntry("Http log will be available at " + httpLog);
        try{
            this.masterId = ServiceManager.startTest(api, this.testId, logger);
        }catch (NumberFormatException e){
            return resultBuilder.failed().build();
        }
        long testInitStart = System.currentTimeMillis();

        String reportUrl=null;
        if (masterId==null||masterId.length() == 0) {
            logger.addErrorLogEntry("Failed to start test.");
            return resultBuilder.failed().build();
        } else {
            reportUrl= ServiceManager.getReportUrl(api,masterId,logger);
            HashMap<String,String> reportUrls= StaticAccessor.getReportUrls();
            reportUrls.put(context.getBuildContext().getBuildResultKey(),reportUrl);
            context.getBuildContext().getBuildResult().getCustomBuildData().put(Constants.REPORT_URL, reportUrl);
        }
        ServiceManager.notes(this.api,masterId,this.notes,logger);

        TestStatus status;
        boolean initTimeOutPassed = false;
        if(!StringUtils.isBlank(this.jmeterProps)){
            JSONArray props=ServiceManager.prepareSessionProperties(this.jmeterProps,logger);
            ServiceManager.properties(this.api,props,masterId,logger);
        }
        do {
            status = this.api.masterStatus(masterId);
            try {
                Thread.currentThread().sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.addErrorLogEntry("BlazeMeter Interrupted Exception: " + e.getMessage());
                logger.addBuildLogEntry("Stopping test...");
                ServiceManager.stopTestMaster(this.api, this.masterId, logger);
                break;
            }
            try {
                this.api.getListOfSessionIds(this.masterId);
                logger.addBuildLogEntry("Check if the test is initialized...");
            } catch (Exception e) {
                logger.addErrorLogEntry(e.getMessage());
                return resultBuilder.failedWithError().build();
            }
            initTimeOutPassed = System.currentTimeMillis() > testInitStart + INIT_TEST_TIMEOUT;
        } while (!(initTimeOutPassed | status.equals(TestStatus.Running)));

        if (status.equals(TestStatus.NotRunning)) {
            logger.addErrorLogEntry("Test was not initialized, marking build as failed.");
            return resultBuilder.failedWithError().build();
        }
        logger.addBuildLogEntry("Test was initialized on server, testId=" + testId);
        logger.addBuildLogEntry("Test report is available via link: " + reportUrl);

        long timeOfStart = System.currentTimeMillis();
        while (status.equals(TestStatus.Running)) {
            try {
                Thread.currentThread().sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.addErrorLogEntry("Received interrupted Exception: " + e.getMessage());
                logger.addBuildLogEntry("Stopping test...");
                ServiceManager.stopTestMaster(this.api, this.masterId, logger);
                break;
            }

            logger.addBuildLogEntry("Check if the test is still running. Time passed since start:" + ((System.currentTimeMillis() - timeOfStart) / 1000 / 60) + " minutes.");
            status = this.api.masterStatus(masterId);
            if (status.equals(TestStatus.NotRunning)) {
                logger.addBuildLogEntry("Test is finished earlier then estimated! Time passed since start:" + ((System.currentTimeMillis() - timeOfStart) / 1000 / 60) + " minutes.");
                break;
            } else if (status.equals(TestStatus.NotFound)) {
                logger.addErrorLogEntry("BlazeMeter test not found!");
                return resultBuilder.failed().build();
            }
        }

        boolean active=true;
        int activeCheck=1;
        while(active&&activeCheck<11){
            try {
                Thread.currentThread().sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.addErrorLogEntry("Thread was interrupted during sleep()");
                logger.addErrorLogEntry("Received interrupted Exception: " + e.getMessage());
                break;
            }
            logger.addBuildLogEntry("Checking, if test is active, testId="+this.testId+", retry # "+activeCheck);
            active=this.api.active(this.testId);
            activeCheck++;
        }
        //BlazeMeter test stopped due to user test duration setup reached

        TestResult result = ServiceManager.getReport(this.api, this.masterId, logger);
        if(this.jtlReport){
            File jtl=null;
            try {
                jtl=ServiceManager.resolvePath(context,this.jtlPath,logger);
            } catch (Exception e) {
                logger.addBuildLogEntry("Failed to create directory for downloading jtl report.");
                jtl=dd;
                logger.addBuildLogEntry("Default directory "+jtl.getAbsolutePath()+" will be used.");
            }
            logger.addBuildLogEntry("Requesting JTL report for test with masterId="+this.masterId);
            ServiceManager.downloadJtlReports(this.api,this.masterId,jtl,logger);
        }else {
            logger.addBuildLogEntry("JTL report won't be requested for test with masterId="+this.masterId);
        }
        if(this.junitReport){
            File junit=null;
            logger.addBuildLogEntry("Requesting Junit report for test with masterId="+this.masterId);
            try {
                junit=ServiceManager.resolvePath(context,this.junitPath,logger);
            } catch (Exception e) {
                logger.addBuildLogEntry("Failed to create directory for downloading junit report.");
                junit=dd;
                logger.addBuildLogEntry("Default directory "+junit.getAbsolutePath()+" will be used.");
            }
            logger.addBuildLogEntry("Requesting JTL report for test with masterId="+this.masterId);
            ServiceManager.downloadJunitReport(this.api,this.masterId,junit,logger);
        }else {
            logger.addBuildLogEntry("Junit report won't be requested for test with masterId="+this.masterId);
        }

        TaskState ciStatus = ServiceManager.ciStatus(this.api, this.masterId, logger);
        switch (ciStatus) {
            case FAILED:
                return resultBuilder.failed().build();
            case ERROR:
                return resultBuilder.failedWithError().build();
            case SUCCESS:
                return resultBuilder.success().build();
            default:
                return resultBuilder.success().build();
        }
    }
}

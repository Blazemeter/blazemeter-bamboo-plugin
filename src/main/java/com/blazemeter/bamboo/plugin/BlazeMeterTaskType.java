package com.blazemeter.bamboo.plugin;

import java.io.File;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.*;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.api.*;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;
import com.blazemeter.bamboo.plugin.testresult.TestResult;
import org.apache.commons.lang3.StringUtils;

public class BlazeMeterTaskType implements TaskType{
	private static final int CHECK_INTERVAL = 60000;
    private static final int INIT_TEST_TIMEOUT = 600000;

    String testId;
    String masterId;
    Api api;

	File rootDirectory;

	ProcessService processService;
	private final PluginSettingsFactory pluginSettingsFactory;

	public BlazeMeterTaskType(final ProcessService processService, PluginSettingsFactory pluginSettingsFactory){
		this.processService = processService;
		this.pluginSettingsFactory = pluginSettingsFactory;
	}

 	@Override
	public TaskResult execute(TaskContext context) throws TaskException {
        final BuildLogger logger = context.getBuildLogger();
        TaskResultBuilder resultBuilder = TaskResultBuilder.create(context);
        ConfigurationMap configMap = context.getConfigurationMap();
        logger.addBuildLogEntry("Executing BlazeMeter task...");
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String userKey = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
        String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
        this.testId = configMap.get(Constants.SETTINGS_SELECTED_TEST_ID);

            if (StringUtils.isBlank(userKey)) {
                logger.addErrorLogEntry("BlazeMeter user key not defined!");
                return resultBuilder.failed().build();
            }
        this.api= new ApiV3Impl(userKey,serverUrl);

        rootDirectory = context.getRootDirectory();
        logger.addBuildLogEntry("Attempting to start test with id:" + testId);
        this.masterId = BzmServiceManager.startTest(api,testId, logger);
        long testInitStart=System.currentTimeMillis();

        if (masterId.length()==0) {
            logger.addErrorLogEntry("Failed to retrieve test masterId id! Check, that test was started correctly on server.");
            return resultBuilder.failed().build();
            } else {
                    context.getBuildContext().getBuildResult().getCustomBuildData().put("session_id", masterId.toString());
            }


            TestStatus status;
        boolean initTimeOutPassed=false;

        do{
            status = this.api.testStatus(masterId);
            try {
                Thread.currentThread().sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.addErrorLogEntry("BlazeMeter Interrupted Exception: " + e.getMessage());
                logger.addBuildLogEntry("Stopping test...");
                BzmServiceManager.stopTestSession(this.api, this.testId, this.masterId, logger);
                break;
            }
            logger.addBuildLogEntry("Check if the test is initialized...");
            initTimeOutPassed=System.currentTimeMillis()>testInitStart+INIT_TEST_TIMEOUT;
        }while (!(initTimeOutPassed|status.equals(TestStatus.Running.toString())));

        if(status.equals(TestStatus.NotRunning.toString())){
            logger.addErrorLogEntry("Test was not initialized, marking build as failed.");
            return resultBuilder.failedWithError().build();
        }
        logger.addBuildLogEntry("Test was initialized on server, testId="+testId);
        logger.addBuildLogEntry("Test report is available via link: "+"https://a.blazemeter.com/app/#reports/"+this.masterId +"/summary");

        long timeOfStart=System.currentTimeMillis();
        while (status.equals(TestStatus.Running.toString())) {
                try {
                    Thread.currentThread().sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    logger.addErrorLogEntry("BlazeMeter Interrupted Exception: " + e.getMessage());
                    logger.addBuildLogEntry("Stopping test...");
                    BzmServiceManager.stopTestSession(this.api, this.testId, this.masterId, logger);
                    break;
                }

                logger.addBuildLogEntry("Check if the test is still running. Time passed since start:" + ((System.currentTimeMillis()-timeOfStart) / 1000 / 60) + " minutes.");
                status = this.api.testStatus(masterId);
                if (status.equals(TestStatus.NotRunning.toString())) {
                    logger.addBuildLogEntry("Test is finished earlier then estimated! Time passed since start:" + ((System.currentTimeMillis()-timeOfStart) / 1000 / 60) + " minutes.");
                    break;
                } else if (status.equals(TestStatus.NotFound.toString())) {
                    logger.addErrorLogEntry("BlazeMeter test not found!");
                    return resultBuilder.failed().build();
                }
            }

            //BlazeMeter test stopped due to user test duration setup reached

            logger.addBuildLogEntry("Test finished. Checking for test report...");
            TestResult result=BzmServiceManager.getReport(this.api, this.masterId, logger);
        TaskState serverTresholdsResult=TaskState.SUCCESS;
        if(this.api instanceof ApiV3Impl){
            serverTresholdsResult=BzmServiceManager.validateServerTresholds(this.api,this.masterId,logger);
        }

        if(serverTresholdsResult.equals(TaskState.FAILED)|serverTresholdsResult.equals(TaskState.ERROR)){
            return resultBuilder.failed().build();
        }else {
            return resultBuilder.success().build();
        }
    }
}

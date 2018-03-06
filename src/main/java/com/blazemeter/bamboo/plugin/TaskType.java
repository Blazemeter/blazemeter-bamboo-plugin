/**
 * Copyright 2016 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.bamboo.plugin;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.configuration.BambooBzmUtils;
import com.blazemeter.bamboo.plugin.configuration.BambooCiBuild;
import com.blazemeter.bamboo.plugin.configuration.BambooCiPostProcess;
import com.blazemeter.bamboo.plugin.configuration.Constants;
import com.blazemeter.bamboo.plugin.logging.AgentUserNotifier;
import com.blazemeter.bamboo.plugin.logging.AgentLogger;

import java.util.List;
import java.util.Map;

import com.blazemeter.bamboo.plugin.servlet.AdminServlet;
import com.blazemeter.ciworkflow.BuildResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TaskType implements com.atlassian.bamboo.task.TaskType {

    ProcessService processService;

    public TaskType(final ProcessService processService) {
        this.processService = processService;
    }

    @Override
    public TaskResult execute(TaskContext context) throws TaskException {
        TaskResultBuilder resultBuilder = TaskResultBuilder.create(context);
        final BuildLogger logger = context.getBuildLogger();
        logger.addBuildLogEntry("Executing BlazeMeter task...");
        logger.addBuildLogEntry("BlazemeterBamboo plugin v." + Utils.getVersion());
        BambooCiBuild build = null;
        BuildResult buildResult;
        try {
            build = setUpCiBuild(context, createLogFile(context));
            buildResult = build.execute();
        } catch (Exception e) {
            logger.addErrorLogEntry("Failed to start build: ",e);
            return resultBuilder.failed().build();
        } finally {
            if (build != null) {
                build.closeLogger();
            }
        }
        switch (buildResult) {
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

    private BambooCiBuild setUpCiBuild(TaskContext context, String logFilePath) throws TaskException {
        ConfigurationMap configMap = context.getConfigurationMap();
        BuildContext buildContext = context.getBuildContext();
        buildContext.getBuildDefinition().getTaskDefinitions().get(0).getPluginKey();
        String testId = Utils.cutTestType(configMap.get(Constants.SETTINGS_SELECTED_TEST_ID));
        final BuildLogger logger = context.getBuildLogger();
        BlazeMeterUtils utils;
        try {
            utils = setUpBzmUtils(context, logFilePath);
        } catch (Exception e) {
            logger.addBuildLogEntry("Failed to find test = " + testId + " on server.");
            throw new TaskException("");
        }
        String jmeterProps = configMap.get(Constants.SETTINGS_JMETER_PROPERTIES);
        boolean jtlReport = configMap.getAsBoolean(Constants.SETTINGS_JTL_REPORT);
        boolean junitReport = configMap.getAsBoolean(Constants.SETTINGS_JUNIT_REPORT);
        String notes = configMap.get(Constants.SETTINGS_NOTES);
        String jtlPath = configMap.get(Constants.SETTINGS_JTL_PATH);
        String junitPath = configMap.get(Constants.SETTINGS_JUNIT_PATH);

        String dd = context.getWorkingDirectory().getAbsolutePath() + "/build # "
                + context.getBuildContext().getBuildNumber();

        BambooCiPostProcess ciPostProcess = new BambooCiPostProcess(jtlReport, junitReport, jtlPath, junitPath, dd, utils.getNotifier(), utils.getLogger());
        return new BambooCiBuild(utils, testId, jmeterProps, notes, ciPostProcess);
    }

    private BambooBzmUtils setUpBzmUtils(TaskContext context, String logFilePath) throws TaskException {
        List<TaskDefinition> tds = context.getBuildContext().getBuildDefinition().getTaskDefinitions();
        final BuildLogger logger = context.getBuildLogger();

        String apiId = null;
        String apiSecret = null;
        String url = null;
        for (TaskDefinition d : tds) {
            if (d.getPluginKey().equals(Constants.PLUGIN_KEY)) {
                Map<String, String> conf = d.getConfiguration();
                apiId = conf.get(AdminServlet.API_ID);
                apiSecret = conf.get(AdminServlet.API_SECRET);
                url = conf.get(AdminServlet.URL);
            }
        }
        if (StringUtils.isBlank(apiId)) {
            logger.addBuildLogEntry("BlazeMeter user key not defined!");
            throw new TaskException("BlazeMeter user key not defined!");
        }
        UserNotifier notifier = new AgentUserNotifier(logger);
        Logger log = new AgentLogger(logFilePath);
        return new BambooBzmUtils(apiId, apiSecret, url, url, notifier, log);
    }

    private String createLogFile(TaskContext context) throws Exception {
        File dd = new File(context.getWorkingDirectory().getAbsolutePath() + "/build # "
                + context.getBuildContext().getBuildNumber());

        final String logFileName = Constants.BZM_LOG + System.currentTimeMillis();

        String logPath = dd + File.separator + logFileName;
        File logFile = new File(logPath);

        BuildLogger buildLogger = context.getBuildLogger();
        try {
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
        } catch (Exception e) {
            buildLogger.addBuildLogEntry("Failed to create log file = " + logPath);
            logFile = new File(context.getWorkingDirectory().getAbsolutePath(), File.separator + logFileName);
            try {
                buildLogger.addBuildLogEntry("Log will be written to " + logFile.getAbsolutePath());
                logFile.createNewFile();
            } catch (Exception ex) {
                buildLogger.addBuildLogEntry("Failed to create log file = " + logFile.getAbsolutePath());
                throw e;
            }
        }
        return logFile.getAbsolutePath();
    }
}

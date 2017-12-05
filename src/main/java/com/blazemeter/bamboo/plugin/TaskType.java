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
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.logging.BzmLogger;
import com.blazemeter.bamboo.plugin.logging.EmptyUserNotifier;

import java.util.List;
import java.util.Map;

import com.blazemeter.ciworkflow.BuildResult;
import com.blazemeter.ciworkflow.CiBuild;
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
        CiBuild build = null;
        try {
            build = setUpCiBuild(context);
        } catch (TaskException e) {
            resultBuilder.failed().build();
        }
        BuildResult buildResult = build.execute();
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

    private CiBuild setUpCiBuild(TaskContext context) throws TaskException {
        ConfigurationMap configMap = context.getConfigurationMap();
        BuildContext buildContext = context.getBuildContext();
        buildContext.getBuildDefinition().getTaskDefinitions().get(0).getPluginKey();
        List<TaskDefinition> tds = buildContext.getBuildDefinition().getTaskDefinitions();
        // TODO
        //extract to setUp BlazeMeterUtils
        String apiId = null;
        String apiSecret = null;
        String url = null;
        for (TaskDefinition d : tds) {
            if (d.getPluginKey().equals(Constants.PLUGIN_KEY)) {
                Map<String, String> conf = d.getConfiguration();
                apiId = conf.get(AdminServletConst.API_ID);
                apiSecret = conf.get(AdminServletConst.API_SECRET);
                url = conf.get(AdminServletConst.URL);
            }
        }
        /////////////////////////////
        String testId = configMap.get(Constants.SETTINGS_SELECTED_TEST_ID);
        final BuildLogger logger = context.getBuildLogger();
        if (StringUtils.isBlank(apiId)) {
            logger.addBuildLogEntry("BlazeMeter user key not defined!");
            throw new TaskException("BlazeMeter user key not defined!");
        }
        //TODO
        UserNotifier notifier = new EmptyUserNotifier();
        //TODO
        // extract to setUpLogger
//        File dd = new File(context.getWorkingDirectory().getAbsolutePath() + "/build # "
//                + context.getBuildContext().getBuildNumber());
//        String httpLog = dd + File.separator + Constants.HTTP_LOG;
//        File httpLog_f = new File(httpLog);
//        try {
//            FileUtils.touch(httpLog_f);
//        } catch (IOException e) {
//            logger.addErrorLogEntry("Failed to create http-log file = " + httpLog + ": " + e.getMessage());
//        }
        Logger log = new BzmLogger();
        BlazeMeterUtils utils = new BlazeMeterUtils(apiId, apiSecret, url, url, notifier, log);
        AbstractTest test;
        try {
            test = TestDetector.detectTest(utils, testId);
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

        File rootDirectory = context.getRootDirectory();

        String dd = context.getWorkingDirectory().getAbsolutePath() + "/build # "
                + context.getBuildContext().getBuildNumber();

        CiBuild build = new CiBuild(test, jmeterProps, notes, jtlReport, junitReport, junitPath, jtlPath, dd);
        return build;
    }
}

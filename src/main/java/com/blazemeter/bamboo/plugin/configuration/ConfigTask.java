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
package com.blazemeter.bamboo.plugin.configuration;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.BuildTaskRequirementSupport;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.logging.BzmLoggerOld;
import com.blazemeter.bamboo.plugin.logging.EmptyUserNotifier;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ConfigTask extends AbstractTaskConfigurator implements BuildTaskRequirementSupport {
    PluginSettingsFactory pluginSettingsFactory;
    BlazeMeterUtils utils;

    public ConfigTask(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    /**
     * first create
     */
    public void populateContextForCreate(Map<String, Object> context) {
        super.populateContextForCreate(context);
        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String apiId = (String) pluginSettings.get(Constants.API_ID);
        String apiSecret = (String) pluginSettings.get(Constants.API_SECRET);
        String url = (String) pluginSettings.get(Constants.URL);
        context.put(Constants.URL, url);
        UserNotifier emptyUserNotifier = new EmptyUserNotifier();
        Logger logger = new BzmLoggerOld();
        utils = new BambooBzmUtils(apiId, apiSecret, url, url, emptyUserNotifier, logger);
        User user = null;
        try {
            user = User.getUser(utils);
            assert user.getId() != null;
            LinkedHashMultimap<String, String> testListDropDown = testsList();
            context.put(Constants.TEST_LIST, testListDropDown.asMap());
        } catch (Exception e) {
            logger.error("Failed to fetch tests for user = " + user.getId(), e);
        }
    }

    /**
     * from backend to view
     */
    public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String psai = (String) pluginSettings.get(Constants.API_ID);
        String psas = (String) pluginSettings.get(Constants.API_SECRET);
        String pssu = (String) pluginSettings.get(Constants.URL);
        Map<String, String> config = taskDefinition.getConfiguration();
        context.put(Constants.API_ID, psai);
        context.put(Constants.API_SECRET, psas);
        context.put(Constants.URL, psai);
        context.put(Constants.SETTINGS_SELECTED_TEST_ID, config.get(Constants.SETTINGS_SELECTED_TEST_ID));
        context.put(Constants.SETTINGS_NOTES, config.get(Constants.SETTINGS_NOTES));
        context.put(Constants.SETTINGS_JUNIT_REPORT, config.get(Constants.SETTINGS_JUNIT_REPORT));
        context.put(Constants.SETTINGS_JTL_REPORT, config.get(Constants.SETTINGS_JTL_REPORT));
        context.put(Constants.SETTINGS_JMETER_PROPERTIES, config.get(Constants.SETTINGS_JMETER_PROPERTIES));
        context.put(Constants.SETTINGS_JTL_PATH, config.get(Constants.SETTINGS_JTL_PATH));
        context.put(Constants.SETTINGS_JUNIT_PATH, config.get(Constants.SETTINGS_JUNIT_PATH));
        UserNotifier emptyUserNotifier = new EmptyUserNotifier();
        Logger logger = new BzmLoggerOld();
        utils = new BambooBzmUtils(psai, psas, pssu, pssu, emptyUserNotifier, logger);
        User user = null;
        try {
            user = User.getUser(utils);
            assert user.getId() != null;
            LinkedHashMultimap<String, String> testListDropDown = testsList();
            context.put(Constants.TEST_LIST, testListDropDown.asMap());
        } catch (Exception e) {
            logger.error("Failed to fetch tests for user = " + user.getId(), e);
        }

    }

    /**
     * Validate gui form when Saving params
     */
    @Override
    public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        final String selectedTest = params.getString(Constants.SETTINGS_SELECTED_TEST_ID);
        if (StringUtils.isEmpty(selectedTest)) {
            errorCollection.addErrorMessage("Check that user has tests in account");
        } else {
            if (selectedTest.contains("workspace")) {
                errorCollection.addErrorMessage("Cannot save workspace as a test. Please, select correct test.");
                return;
            }
            try {
                if (StringUtils.isBlank(User.getUser(utils).getId())) {
                    errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
                } else {
                    //verify if the test still exists on BlazeMeter server
                    AbstractTest receivedTest = TestDetector.detectTest(utils, selectedTest);
                    if (receivedTest == null) {
                        errorCollection.addErrorMessage("Test '" + selectedTest + "' doesn't exits on BlazeMeter server.");
                    }
                }
            } catch (Exception e) {
                errorCollection.addErrorMessage("Failed to get tests from BlazeMeter account: " + e.getMessage());
            }
        }
    }

    /**
     * from gui to backend
     */
    @Override
    public Map<String, String> generateTaskConfigMap(ActionParametersMap params, TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(Constants.SETTINGS_SELECTED_TEST_ID, params.getString(Constants.SETTINGS_SELECTED_TEST_ID).trim());
        config.put(Constants.SETTINGS_JMETER_PROPERTIES, params.getString(Constants.SETTINGS_JMETER_PROPERTIES).trim());
        config.put(Constants.SETTINGS_NOTES, params.getString(Constants.SETTINGS_NOTES).trim());
        config.put(Constants.SETTINGS_JTL_PATH, params.getString(Constants.SETTINGS_JTL_PATH).trim());
        config.put(Constants.SETTINGS_JUNIT_PATH, params.getString(Constants.SETTINGS_JUNIT_PATH).trim());
        String jtlReport = params.getString(Constants.SETTINGS_JTL_REPORT) == null ? "false" : "true";
        String junitReport = params.getString(Constants.SETTINGS_JUNIT_REPORT) == null ? "false" : "true";
        config.put(Constants.SETTINGS_JTL_REPORT, jtlReport);
        config.put(Constants.SETTINGS_JUNIT_REPORT, junitReport);

        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        config.put(Constants.API_ID,
                (String) pluginSettings.get(Constants.API_ID));
        config.put(Constants.API_SECRET,
                (String) pluginSettings.get(Constants.API_SECRET));
        config.put(Constants.URL,
                (String) pluginSettings.get(Constants.URL));
        return config;
    }

    private LinkedHashMultimap<String, String> testsList() throws Exception {
        LinkedHashMultimap<String, String> testListDropDown = LinkedHashMultimap.create();
        User user = User.getUser(utils);
        List<Account> accounts = user.getAccounts();
        for (Account a : accounts) {
            List<Workspace> workspaces = a.getWorkspaces();
            for (Workspace wsp : workspaces) {
                List<AbstractTest> tests = new ArrayList<>();
                tests.addAll(wsp.getMultiTests());
                tests.addAll(wsp.getSingleTests());
                Comparator c = new Comparator<AbstractTest>() {
                    @Override
                    public int compare(AbstractTest t1, AbstractTest t2) {
                        return t1.getName().compareToIgnoreCase(t2.getName());
                    }
                };
                tests.sort(c);
                testListDropDown.put("workspace", wsp.getName() + "(" + wsp.getId() + ")");
                for (AbstractTest t : tests) {
                    testListDropDown.put(t.getId(), t.getName() + "(" + t.getId() + "." + t.getTestType() + ")");
                }
                tests.clear();
            }
        }
        return testListDropDown;
    }
}

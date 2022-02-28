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
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.Utils;
import com.blazemeter.bamboo.plugin.logging.ServerLogger;
import com.blazemeter.bamboo.plugin.logging.ServerUserNotifier;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConfigTask extends AbstractTaskConfigurator implements BuildTaskRequirementSupport {
    PluginSettingsFactory pluginSettingsFactory;
    BlazeMeterUtils utils;
    String CHECK_CREDENTIALS = "Check that credentials are valid";
    String CHECK_TESTS = " and there are tests.";
    String WORKSPACE = "workspace";
    String NO_TESTS = "no-tests";

    public ConfigTask(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    /**
     * first create
     */
    public void populateContextForCreate(Map<String, Object> context) {
        super.populateContextForCreate(context);

        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String apiId = (String) pluginSettings.get(AdminServlet.API_ID);
        String apiSecret = (String) pluginSettings.get(AdminServlet.API_SECRET);
        String url = (String) pluginSettings.get(AdminServlet.URL);
        UserNotifier serverUserNotifier = new ServerUserNotifier();
        Logger logger = new ServerLogger();
        utils = new BambooBzmUtils(apiId, apiSecret, url, url, serverUserNotifier, logger);
        fillContextWithTests(context, null);
        context.put(AdminServlet.URL, url);
        logger.info("New BlazeMeter task is opened for configuration.");
    }

    /**
     * from backend to view
     */
    public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String psai = (String) pluginSettings.get(AdminServlet.API_ID);
        String psas = (String) pluginSettings.get(AdminServlet.API_SECRET);
        String pssu = (String) pluginSettings.get(AdminServlet.URL);
        Map<String, String> config = taskDefinition.getConfiguration();
        context.put(AdminServlet.API_ID, psai);
        context.put(AdminServlet.API_SECRET, psas);
        context.put(AdminServlet.URL, psai);
        context.put(Constants.SETTINGS_SELECTED_TEST_ID, config.get(Constants.SETTINGS_SELECTED_TEST_ID));
        context.put(Constants.SETTINGS_NOTES, config.get(Constants.SETTINGS_NOTES));
        context.put(Constants.SETTINGS_JUNIT_REPORT, config.get(Constants.SETTINGS_JUNIT_REPORT));
        context.put(Constants.SETTINGS_JTL_REPORT, config.get(Constants.SETTINGS_JTL_REPORT));
        context.put(Constants.SETTINGS_JMETER_PROPERTIES, config.get(Constants.SETTINGS_JMETER_PROPERTIES));
        context.put(Constants.SETTINGS_JTL_PATH, config.get(Constants.SETTINGS_JTL_PATH));
        context.put(Constants.SETTINGS_JUNIT_PATH, config.get(Constants.SETTINGS_JUNIT_PATH));
        context.put(Constants.SETTINGS_SELECTED_WORKSPACE_ID,config.get(Constants.SETTINGS_SELECTED_WORKSPACE_ID));

        UserNotifier serverUserNotifier = new ServerUserNotifier();
        Logger logger = new ServerLogger();
        utils = new BambooBzmUtils(psai, psas, pssu, pssu, serverUserNotifier, logger);
        logger.info("BlazeMeter task is opened for configuration.");
        fillContextWithTests(context, config.get("selectedtest"));
    }

    /**
     * Validate gui form when Saving params
     */
    @Override
    public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        utils.getNotifier().notifyInfo("Validating BlazeMeter task settings before saving.");
        final String selectedTest = Utils.cutTestType(params.getString(Constants.SETTINGS_SELECTED_TEST_ID));
        fillErrorCollection(selectedTest, errorCollection);
        if (errorCollection.hasAnyErrors()) {
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
        utils.getNotifier().notifyInfo("BlazeMeter task settings were validated and saved.");
    }

    private void fillContextWithTests(Map<String, Object> context, String selectedTest) {
        Map<String, String> testListDropDown = new HashMap<>();
        Map<String, String> workspacesDropDown = new TreeMap<>();
        Map<String, Map<String, String>> wspMap = new HashMap<>();
        try {
            User user = User.getUser(utils);
            assert user.getId() != null;
            String wspForSelect = fillAccountInfo(user, selectedTest, wspMap, workspacesDropDown, testListDropDown);
            context.put("savedWsp", wspForSelect);
            context.put("savedTest", selectedTest);

        } catch (Exception e) {
            utils.getLogger().error("Failed to get user: " + e);
            testListDropDown.put(CHECK_CREDENTIALS, CHECK_CREDENTIALS);
            workspacesDropDown.put(CHECK_CREDENTIALS, CHECK_CREDENTIALS);
            wspMap.put(CHECK_CREDENTIALS, testListDropDown);

            context.put("savedWsp", CHECK_CREDENTIALS);
            context.put("savedTest", CHECK_CREDENTIALS);
        } finally {
            context.put(Constants.TEST_LIST, testListDropDown);
            context.put("workspaceList", workspacesDropDown);
            context.put("wspMap", wspMap);
        }
    }

    private void fillErrorCollection(String selectedTest, ErrorCollection errorCollection) {
        if (StringUtils.isBlank(selectedTest) || selectedTest.contains(CHECK_CREDENTIALS)) {
            errorCollection.addErrorMessage(CHECK_CREDENTIALS + CHECK_TESTS);
            return;
        }
        if (selectedTest.contains(WORKSPACE)) {
            errorCollection.addErrorMessage("Cannot save workspace as a test. Please, select correct test.");
            return;
        }
        if (selectedTest.contains(NO_TESTS)) {
            errorCollection.addErrorMessage("No tests in current workspace. Please, select correct test.");
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
        config.put(Constants.SETTINGS_SELECTED_WORKSPACE_ID,params.getString(Constants.SETTINGS_SELECTED_WORKSPACE_ID).trim());

        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        config.put(AdminServlet.API_ID,
                (String) pluginSettings.get(AdminServlet.API_ID));
        config.put(AdminServlet.API_SECRET,
                (String) pluginSettings.get(AdminServlet.API_SECRET));
        config.put(AdminServlet.URL,
                (String) pluginSettings.get(AdminServlet.URL));
        return config;
    }

    private void addMultiTests(Workspace wsp, List<AbstractTest> tests) {
        try {
            tests.addAll(wsp.getMultiTests());
        } catch (Exception e) {
            wsp.getUtils().getNotifier().notifyError("Failed to get multi-tests for workspace = " + wsp.getId());
            tests.add(new MultiTest(utils, "", "Failed to load multi-tests", ""));
        }
    }

    private void addSingleTests(Workspace wsp, List<AbstractTest> tests) {
        try {
            tests.addAll(wsp.getSingleTests());
        } catch (Exception e) {
            wsp.getUtils().getNotifier().notifyError("Failed to get single-tests for workspace = " + wsp.getId());
            tests.add(new SingleTest(utils, "", "Failed to load single-tests", ""));
        }
    }


    public String fillAccountInfo(User user, String selectedTest,
                                  Map<String, Map<String, String>> wspMap,
                                  Map<String, String> workspacesDropDown,
                                  Map<String, String> testListDropDown) throws Exception {

        String wspForSelect = null;

        List<Account> accounts = user.getAccounts();
        for (Account a : accounts) {

            List<Workspace> workspaces = a.getWorkspaces();
            List<AbstractTest> tests = new ArrayList<>();
            for (Workspace wsp : workspaces) {
                workspacesDropDown.put(WORKSPACE + wsp.getId(), wsp.getName() + "(" + wsp.getId() + ")");
                tests.clear();
                addMultiTests(wsp, tests);
                addSingleTests(wsp, tests);

                Map<String, String> testsList = new HashMap<>();
                if (tests.isEmpty()) {
                    testsList.put("no-tests" + workspaces.indexOf(wsp), "No tests in workspace");
                    wspMap.put(WORKSPACE + wsp.getId(), testsList);
                    continue;
                }

                boolean isSelectedWorkspace = false;
                for (AbstractTest t : tests) {
                    String testIdType = t.getId() + "." + t.getTestType();
                    testsList.put(testIdType, t.getName() + "(" + testIdType + ")");
                    if (testIdType.equals(selectedTest)) {
                        isSelectedWorkspace = true;
                        wspForSelect = WORKSPACE + wsp.getId();
                    }
                }
                wspMap.put(WORKSPACE + wsp.getId(), testsList);
                if (isSelectedWorkspace) {
                    testListDropDown.putAll(testsList);
                }

            }
        }
        return wspForSelect;
    }
}
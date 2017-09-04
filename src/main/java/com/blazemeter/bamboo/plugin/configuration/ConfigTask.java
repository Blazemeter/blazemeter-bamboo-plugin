/**
 * Copyright 2016 BlazeMeter Inc.
 *
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
import com.blazemeter.bamboo.plugin.ServiceManager;
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.ApiImpl;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import okhttp3.Credentials;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class ConfigTask extends AbstractTaskConfigurator implements BuildTaskRequirementSupport {

    private static final List<String> FIELDS_TO_COPY =
        ImmutableList.of(Constants.SETTINGS_SELECTED_TEST_ID,
            Constants.SETTINGS_JTL_REPORT,
            Constants.SETTINGS_JUNIT_REPORT,
            Constants.SETTINGS_JMETER_PROPERTIES,
            Constants.SETTINGS_NOTES,
            Constants.SETTINGS_JTL_PATH,
            Constants.SETTINGS_JUNIT_PATH);
    private Api api;

//    private TextProvider textProvider;

    PluginSettingsFactory pluginSettingsFactory;

    public ConfigTask(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    /**
     * first create
     */
    public void populateContextForCreate(Map<String, Object> context) {
        super.populateContextForCreate(context);
        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String api_id = (String) pluginSettings.get(AdminServletConst.API_ID);
        String api_secret = (String) pluginSettings.get(AdminServletConst.API_SECRET);
        String serverUrl = (String) pluginSettings.get(AdminServletConst.URL);
        context.put(AdminServletConst.URL, serverUrl);
        String credentials = Credentials.basic(api_id,api_secret);
        this.api = new ApiImpl(credentials, serverUrl);
        context.put(Constants.TEST_LIST, api.getTestsMultiMap());
    }

    /**
     * from backend to view
     */
    public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String psai = (String) pluginSettings.get(AdminServletConst.API_ID);
        String psas = (String) pluginSettings.get(AdminServletConst.API_SECRET);
        String pssu = (String) pluginSettings.get(AdminServletConst.URL);
        Map<String, String> config = taskDefinition.getConfiguration();
        context.put(AdminServletConst.API_ID, psai);
        context.put(AdminServletConst.API_SECRET, psas);
        context.put(AdminServletConst.URL, psai);
        context.put(Constants.SETTINGS_SELECTED_TEST_ID, config.get(Constants.SETTINGS_SELECTED_TEST_ID));
        context.put(Constants.SETTINGS_NOTES, config.get(Constants.SETTINGS_NOTES));
        context.put(Constants.SETTINGS_JUNIT_REPORT, config.get(Constants.SETTINGS_JUNIT_REPORT));
        context.put(Constants.SETTINGS_JTL_REPORT, config.get(Constants.SETTINGS_JTL_REPORT));
        context.put(Constants.SETTINGS_JMETER_PROPERTIES, config.get(Constants.SETTINGS_JMETER_PROPERTIES));
        context.put(Constants.SETTINGS_JTL_PATH, config.get(Constants.SETTINGS_JTL_PATH));
        context.put(Constants.SETTINGS_JUNIT_PATH, config.get(Constants.SETTINGS_JUNIT_PATH));
        String credentials = Credentials.basic(psai, psas);
        this.api = new ApiImpl(credentials, pssu);
        try {
            context.put(Constants.TEST_LIST, api.getTestsMultiMap());
        } catch (Exception e) {
            LinkedHashMultimap<String, String> tests = LinkedHashMultimap.create();
            tests.put("Check blazemeter & proxy-settings", "");
            context.put(Constants.TEST_LIST, tests);
        }
    }

    @Override
    public void populateContextForView(Map<String, Object> context, TaskDefinition taskDefinition) {
        context.put(AdminServletConst.API_ID,
            taskDefinition.getConfiguration().get(AdminServletConst.API_ID));
        context.put(AdminServletConst.API_SECRET,
            taskDefinition.getConfiguration().get(AdminServletConst.API_SECRET));
        context.put(AdminServletConst.URL,
            taskDefinition.getConfiguration().get(AdminServletConst.URL));
        super.populateContextForView(context, taskDefinition);
    }

    /**
     * Validate gui form when Saving params
     */
    @Override
    public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        LinkedHashMultimap<String, String> tests=LinkedHashMultimap.create();
        final String selectedTest = params.getString(Constants.SETTINGS_SELECTED_TEST_ID);

        if (StringUtils.isEmpty(selectedTest)) {
            errorCollection.addErrorMessage("Check that user has tests in account");
        } else {
            if (!this.api.verifyCredentials()) {
                errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
            } else {
                //verify if the test still exists on BlazeMeter server
                try{
                    tests = api.testsMultiMap();

                }catch (Exception e){
                    errorCollection.addErrorMessage("Failed to get tests from BlazeMeter account: "+e.getMessage());
                }
                if (tests != null) {
                    if (!tests.keySet().contains(selectedTest)) {
                        errorCollection.addErrorMessage("Test '" + selectedTest + "' doesn't exits on BlazeMeter server.");
                    }
                } else {
                    errorCollection.addErrorMessage("No tests defined on BlazeMeter server!");
                }
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
        config.put(AdminServletConst.API_ID,
            (String) pluginSettings.get(AdminServletConst.API_ID));
        config.put(AdminServletConst.API_SECRET,
            (String) pluginSettings.get(AdminServletConst.API_SECRET));
        config.put(AdminServletConst.URL,
            (String) pluginSettings.get(AdminServletConst.URL));
        return config;
    }
}

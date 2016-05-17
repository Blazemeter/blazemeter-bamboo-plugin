package com.blazemeter.bamboo.plugin.configuration;

import java.util.List;
import java.util.Map;

import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.ApiV3Impl;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang.StringUtils;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.BuildTaskRequirementSupport;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.ServiceManager;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;
import com.google.common.collect.ImmutableList;
import com.opensymphony.xwork.TextProvider;

public class ConfigTask extends AbstractTaskConfigurator implements BuildTaskRequirementSupport{

	private static final List<String> FIELDS_TO_COPY =
			ImmutableList.of(Constants.SETTINGS_SELECTED_TEST_ID,
					Constants.SETTINGS_JTL_REPORT,
					Constants.SETTINGS_JUNIT_REPORT,
					Constants.SETTINGS_JMETER_PROPERTIES);
	private Api api;

	private TextProvider textProvider;
	
	public ConfigTask() {
		super();
	}

	@Override
	public void populateContextForCreate(Map<String, Object> context) {
		super.populateContextForCreate(context);
        PluginSettingsFactory pluginSettingsFactory=StaticAccessor.getSettingsFactory();
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
		String userKey = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
		String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
		context.put(AdminServletConst.URL, serverUrl);
		this.api= new ApiV3Impl(userKey,serverUrl);
		context.put(Constants.TEST_LIST, ServiceManager.getTestsAsMap(api));
	}

	@Override
	public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
        PluginSettingsFactory pluginSettingsFactory=StaticAccessor.getSettingsFactory();
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
		String userKey = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
		String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
		context.put(AdminServletConst.URL, serverUrl);
		this.api= new ApiV3Impl(userKey,serverUrl);
        try{
			context.put(Constants.TEST_LIST, ServiceManager.getTestsAsMap(this.api));
		}catch (Exception e){
			LinkedHashMultimap<String,String> tests= LinkedHashMultimap.create();
			tests.put("Check blazemeter & proxy-settings","");
			context.put(Constants.TEST_LIST, tests);
		}
	}

	@Override
	public void populateContextForView(Map<String, Object> context, TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
	}

	@Override
	public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
		super.validate(params, errorCollection);

		final String selectedTest = params.getString(Constants.SETTINGS_SELECTED_TEST_ID);

        if (StringUtils.isEmpty(this.api.getUserKey())) {
			errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
		}

		if (StringUtils.isEmpty(selectedTest)) {
			errorCollection.addErrorMessage(textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_SELECTED_TEST_ID));
		} else {
			if (!this.api.verifyUserKey()){
				errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
			} else {
				//verify if the test still exists on BlazeMeter server
                LinkedHashMultimap<String, String> tests = ServiceManager.getTests(this.api);
				if (tests != null){
					if (!tests.keySet().contains(selectedTest)) {
						errorCollection.addErrorMessage("Test '"+selectedTest+"' doesn't exits on BlazeMeter server.");
					}
				} else {
					errorCollection.addErrorMessage("No tests defined on BlazeMeter server!");
				}
			}
		}
}


	
	@Override
	public Map<String, String> generateTaskConfigMap(ActionParametersMap params, TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(Constants.SETTINGS_SELECTED_TEST_ID, params.getString(Constants.SETTINGS_SELECTED_TEST_ID).trim());
		config.put(Constants.SETTINGS_JMETER_PROPERTIES, params.getString(Constants.SETTINGS_JMETER_PROPERTIES).trim());
		String jtlReport=params.getString(Constants.SETTINGS_JTL_REPORT)==null?"false":"true";
		String junitReport=params.getString(Constants.SETTINGS_JUNIT_REPORT)==null?"false":"true";
		config.put(Constants.SETTINGS_JTL_REPORT, jtlReport);
		config.put(Constants.SETTINGS_JUNIT_REPORT, junitReport);
		return config;
	}

	public void setTextProvider(final TextProvider textProvider) {
		this.textProvider = textProvider;
	}
}

package com.blazemeter.bamboo.plugin.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blazemeter.bamboo.plugin.Utils;
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
import com.blazemeter.bamboo.plugin.api.BzmServiceManager;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;
import com.google.common.collect.ImmutableList;
import com.opensymphony.xwork.TextProvider;

public class BlazeMeterConfigTask extends AbstractTaskConfigurator implements BuildTaskRequirementSupport{

	private static final List<String> API_VERSION_LIST = ImmutableList.of("autoDetect","v3","v2");
	private static final List<String> FIELDS_TO_COPY = ImmutableList.of(Constants.SETTINGS_SELECTED_TEST_ID,
			Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE, Constants.SETTINGS_ERROR_THRESHOLD_FAIL,
			Constants.SETTINGS_RESPONSE_TIME_UNSTABLE, Constants.SETTINGS_RESPONSE_TIME_FAIL,
			Constants.SETTINGS_TEST_DURATION, Constants.SETTINGS_DATA_FOLDER,
			Constants.SETTINGS_MAIN_JMX);


	private TextProvider textProvider;
	
	public BlazeMeterConfigTask() {
		super();
	}

	@Override
	public void populateContextForCreate(Map<String, Object> context) {
		super.populateContextForCreate(context);
        PluginSettingsFactory pluginSettingsFactory=StaticAccessor.getSettingsFactory();
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
		String apiVersion = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_API_VERSION);
		context.put(AdminServletConst.URL, serverUrl);
		context.put(AdminServletConst.API_VERSION, apiVersion);
		context.put(Constants.SETTINGS_DATA_FOLDER, Constants.DEFAULT_SETTINGS_DATA_FOLDER);
        BzmServiceManager bzmServiceManager=BzmServiceManager.getBzmServiceManager(context);

        setSessionId(bzmServiceManager);
		context.put(Constants.TEST_LIST, bzmServiceManager.getTestsAsMap());
	}

	@Override
	public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
        PluginSettingsFactory pluginSettingsFactory=StaticAccessor.getSettingsFactory();
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
        String apiVersion = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_API_VERSION);
        context.put(AdminServletConst.URL, serverUrl);
        context.put(AdminServletConst.API_VERSION, apiVersion);

        BzmServiceManager bzmServiceManager=BzmServiceManager.getBzmServiceManager(context);
		setSessionId(bzmServiceManager);
		context.put(Constants.TEST_LIST, bzmServiceManager.getTestsAsMap());

		context.put(Constants.SETTINGS_DATA_FOLDER, taskDefinition.getConfiguration().get(Constants.SETTINGS_DATA_FOLDER));
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
		final String errorUnstable = params.getString(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		final String errorFail = params.getString(Constants.SETTINGS_ERROR_THRESHOLD_FAIL);
		final String respUnstable = params.getString(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		final String respFail = params.getString(Constants.SETTINGS_RESPONSE_TIME_FAIL);
		final String testDuration = params.getString(Constants.SETTINGS_TEST_DURATION);


//		final String dataFolder = params.getString(BlazeMeterConstants.SETTINGS_DATA_FOLDER);
//		final String mainJMX = params.getString(BlazeMeterConstants.SETTINGS_MAIN_JMX);


        PluginSettingsFactory pluginSettingsFactory=StaticAccessor.getSettingsFactory();
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String userKey = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
        String serverUrl = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
        String proxyserver = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_PROXY_SERVER);
        String proxyport = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_PROXY_PORT);
        String proxyuser = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_PROXY_USER);
        String proxypass = (String) pluginSettings.get(Config.class.getName() + Constants.TEST_LIST);
        String apiVersion = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_API_VERSION);

        BzmServiceManager bzmServiceManager= BzmServiceManager.getBzmServiceManager(serverUrl,
                proxyserver,
                proxyport,
                proxyuser,
                proxypass,
                apiVersion);


        if (StringUtils.isEmpty(bzmServiceManager.getUserKey())) {
			errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
		}

		if (StringUtils.isEmpty(selectedTest)) {
			errorCollection.addErrorMessage(textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_SELECTED_TEST_ID));
		} else {
			if (!bzmServiceManager.verifyUserKey(bzmServiceManager.getUserKey())){
				errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
			} else {
				//verify if the test still exists on BlazeMeter server
                LinkedHashMultimap<String, String> tests = bzmServiceManager.getTests();
				if (tests != null){
					if (!tests.keySet().contains(selectedTest)) {
						errorCollection.addErrorMessage("Test '"+selectedTest+"' doesn't exits on BlazeMeter server.");
					}
				} else {
					errorCollection.addErrorMessage("No tests defined on BlazeMeter server!");
				}
			}
		}
		
		if (!StringUtils.isEmpty(errorUnstable)&&!Utils.checkNumber(errorUnstable, true)) {
            errorCollection.addError(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
                    textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE));
		}

		if (!StringUtils.isEmpty(errorFail)&&!Utils.checkNumber(errorFail, true)) {
            errorCollection.addError(Constants.SETTINGS_ERROR_THRESHOLD_FAIL,
                    textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_ERROR_THRESHOLD_FAIL));

        }

		if (!StringUtils.isEmpty(respUnstable)&&!Utils.checkNumber(respUnstable, false)) {
			errorCollection.addError(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE,
						textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_RESPONSE_TIME_UNSTABLE));
		}
		

        if (!StringUtils.isEmpty(respFail)&&!Utils.checkNumber(respFail, false)) {
				errorCollection.addError(Constants.SETTINGS_RESPONSE_TIME_FAIL,
						textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_RESPONSE_TIME_FAIL));
		}


		if (!StringUtils.isEmpty(testDuration)&&!Utils.checkNumber(testDuration, false)) {
			errorCollection.addError(Constants.SETTINGS_TEST_DURATION,
					textProvider.getText(Constants.BLAZEMETER_ERROR + Constants.SETTINGS_TEST_DURATION));
		}
	}


	
	@Override
	public Map<String, String> generateTaskConfigMap(ActionParametersMap params, TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(Constants.SETTINGS_SELECTED_TEST_ID, params.getString(Constants.SETTINGS_SELECTED_TEST_ID).trim());
		config.put(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE, params.getString(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE).trim());
		config.put(Constants.SETTINGS_ERROR_THRESHOLD_FAIL, params.getString(Constants.SETTINGS_ERROR_THRESHOLD_FAIL).trim());
		config.put(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE, params.getString(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE).trim());
		config.put(Constants.SETTINGS_RESPONSE_TIME_FAIL, params.getString(Constants.SETTINGS_RESPONSE_TIME_FAIL).trim());
		config.put(Constants.SETTINGS_TEST_DURATION, params.getString(Constants.SETTINGS_TEST_DURATION).trim());
		config.put(Constants.SETTINGS_DATA_FOLDER, params.getString(Constants.SETTINGS_DATA_FOLDER).trim());
		config.put(Constants.SETTINGS_MAIN_JMX, params.getString(Constants.SETTINGS_MAIN_JMX).trim());

		return config;
	}

	public void setTextProvider(final TextProvider textProvider) {
		this.textProvider = textProvider;
	}

	public void setSessionId(BzmServiceManager bzmServiceManager){
		final PluginSettingsFactory pluginSettingsFactory = StaticAccessor.getSettingsFactory();
		PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();	
		String config = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
        if (config != null){
			bzmServiceManager.setUserKey(config);
		}
	}

}

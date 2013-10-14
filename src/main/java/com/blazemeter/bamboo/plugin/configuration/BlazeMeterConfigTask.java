package com.blazemeter.bamboo.plugin.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.BuildTaskRequirementSupport;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.api.BlazeBean;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;
import com.google.common.collect.ImmutableList;
import com.opensymphony.xwork.TextProvider;

public class BlazeMeterConfigTask extends AbstractTaskConfigurator implements BuildTaskRequirementSupport{

	BlazeBean blazeBean;
	
	private static final List<String> FIELDS_TO_COPY = ImmutableList.of(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID,
			BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE, BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL,
			BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE, BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL,
			BlazeMeterConstants.SETTINGS_TEST_DURATION, BlazeMeterConstants.SETTINGS_DATA_FOLDER,
			BlazeMeterConstants.SETTINGS_MAIN_JMX);

	private static final List<String> DURATION_LIST = ImmutableList.of("60", "120", "180");
	
	private TextProvider textProvider;
	
	public BlazeMeterConfigTask() {
		super();
		blazeBean = new BlazeBean();
	}

	@Override
	public void populateContextForCreate(Map<String, Object> context) {
		super.populateContextForCreate(context);
		context.put(BlazeMeterConstants.SETTINGS_DATA_FOLDER, BlazeMeterConstants.DEFAULT_SETTINGS_DATA_FOLDER);
		
		setSessionId();
		context.put("testlist", blazeBean.getTests());
		context.put("durationlist", DURATION_LIST);
	}

	@Override
	public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);

		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
		
		setSessionId();
		context.put("testlist", blazeBean.getTests());
		
		context.put("durationlist", DURATION_LIST);
		
		context.put(BlazeMeterConstants.SETTINGS_DATA_FOLDER, taskDefinition.getConfiguration().get(BlazeMeterConstants.SETTINGS_DATA_FOLDER));
	}

	@Override
	public void populateContextForView(Map<String, Object> context, TaskDefinition taskDefinition) {
		super.populateContextForView(context, taskDefinition);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
	}

	@Override
	public void validate(ActionParametersMap params, ErrorCollection errorCollection) {
		super.validate(params, errorCollection);

		final String selectedTest = params.getString(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID);
		final String errorUnstable = params.getString(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		final String errorFail = params.getString(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL);
		final String respUnstable = params.getString(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		final String respFail = params.getString(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL);
		final String testDuration = params.getString(BlazeMeterConstants.SETTINGS_TEST_DURATION);
//		final String dataFolder = params.getString(BlazeMeterConstants.SETTINGS_DATA_FOLDER);
//		final String mainJMX = params.getString(BlazeMeterConstants.SETTINGS_MAIN_JMX);

		
		if (StringUtils.isEmpty(blazeBean.getUserKey())) {
			errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
		}

		if (StringUtils.isEmpty(selectedTest)) {
			errorCollection.addErrorMessage(textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID));
		} else {
			if (!blazeBean.verifyUserKey(blazeBean.getUserKey())){
				errorCollection.addErrorMessage("Cannot load tests from BlazeMeter server. Invalid user key!");
			} else {
				//verify if the test still exists on BlazeMeter server
				HashMap<String, String> tests = blazeBean.getTests();
				if (tests != null){
					if (!tests.keySet().contains(selectedTest)) {
						errorCollection.addErrorMessage("Test '"+selectedTest+"' doesn't exits on BlazeMeter server.");
					}
				} else {
					errorCollection.addErrorMessage("No tests defined on BlazeMeter server!");
				}
			}
		}
		
		if (StringUtils.isEmpty(errorUnstable)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE));
		} else {
			if (!checkNumber(errorUnstable, true)) {
				errorCollection.addError(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
						textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE));
			}
		}
		
		if (StringUtils.isEmpty(errorFail)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL));
		} else {
			if (!checkNumber(errorFail, true)) {
				errorCollection.addError(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL,
						textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL));
			}
		}
		
		if (StringUtils.isEmpty(respUnstable)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE));
		} else {
			if (!checkNumber(respUnstable, false)) {
				errorCollection.addError(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE,
						textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE));
			}
		}
		
		if (StringUtils.isEmpty(respFail)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL));
		} else {
			if (!checkNumber(respFail, false)) {
				errorCollection.addError(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL,
						textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL));
			}
		}
		
		if (StringUtils.isEmpty(testDuration)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_TEST_DURATION,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_TEST_DURATION));
		}
	}

	private boolean checkNumber(String number, boolean isPercentage){
		try{
			if (number.equals("-0")){
				throw new NumberFormatException("Value cannot be -0!");
			}
			Integer val = Integer.valueOf(number);
			if (isPercentage){
				if (!((val >= 0) && (val <= 100))){
					throw new NumberFormatException("Value is not between 0 and 100!");
				}
			} else {
				if (!(val >= 0)){
					throw new NumberFormatException("Value must be greater than 0!");
				}
			}
		} catch (NumberFormatException nfe){
			return false;
		}
		
		return true;
	}
	
	@Override
	public Map<String, String> generateTaskConfigMap(ActionParametersMap params, TaskDefinition previousTaskDefinition) {
		final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

		config.put(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID, params.getString(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID).trim());
		config.put(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE, params.getString(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE).trim());
		config.put(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL, params.getString(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL).trim());
		config.put(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE, params.getString(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE).trim());
		config.put(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL, params.getString(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL).trim());
		config.put(BlazeMeterConstants.SETTINGS_TEST_DURATION, params.getString(BlazeMeterConstants.SETTINGS_TEST_DURATION).trim());
		config.put(BlazeMeterConstants.SETTINGS_DATA_FOLDER, params.getString(BlazeMeterConstants.SETTINGS_DATA_FOLDER).trim());
		config.put(BlazeMeterConstants.SETTINGS_MAIN_JMX, params.getString(BlazeMeterConstants.SETTINGS_MAIN_JMX).trim());

		return config;
	}

	public void setTextProvider(final TextProvider textProvider) {
		this.textProvider = textProvider;
	}

	public void setSessionId(){
		final PluginSettingsFactory pluginSettingsFactory = StaticAccessor.getSettingsFactory();
		PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();	
		String config = (String) pluginSettings.get(Config.class.getName() + ".userkey");
		String proxyserver = (String) pluginSettings.get(Config.class.getName() + ".proxyserver");
		String proxyport = (String) pluginSettings.get(Config.class.getName() + ".proxyport");
		String proxyuser = (String) pluginSettings.get(Config.class.getName() + ".proxyuser");
		String proxypass = (String) pluginSettings.get(Config.class.getName() + ".proxypass");		
		if (config != null){
			blazeBean.setUserKey(config);
		}
		if (proxyserver != null){
			blazeBean.setProxyserver(proxyserver);
		} 
		if (proxyport != null){
			blazeBean.setProxyport(proxyport);
		} 
		if (proxyuser != null){
			blazeBean.setProxyuser(proxyuser);
		} 
		if (proxypass != null){
			blazeBean.setProxypass(proxypass);
		} 
		
	}

}

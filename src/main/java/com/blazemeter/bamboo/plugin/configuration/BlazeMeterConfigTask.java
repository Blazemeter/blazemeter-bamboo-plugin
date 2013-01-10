package com.blazemeter.bamboo.plugin.configuration;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.blazemeter.bamboo.plugin.api.BlazeBean;
import com.google.common.collect.ImmutableList;
import com.opensymphony.xwork.TextProvider;

public class BlazeMeterConfigTask extends AbstractTaskConfigurator {

	private final String USER_KEY = "5a8da32f36036f8c29fe";
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
		blazeBean.setUserKey(USER_KEY);		
	}

	@Override
	public void populateContextForCreate(Map<String, Object> context) {
		super.populateContextForCreate(context);
		context.put(BlazeMeterConstants.SETTINGS_DATA_FOLDER, BlazeMeterConstants.DEFAULT_SETTINGS_DATA_FOLDER);
		
		context.put("testlist", blazeBean.getTests());
		context.put("durationlist", DURATION_LIST);
	}

	@Override
	public void populateContextForEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
		super.populateContextForEdit(context, taskDefinition);
		taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELDS_TO_COPY);
		
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
		final String dataFolder = params.getString(BlazeMeterConstants.SETTINGS_DATA_FOLDER);
		final String mainJMX = params.getString(BlazeMeterConstants.SETTINGS_MAIN_JMX);

		if (StringUtils.isEmpty(selectedTest)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID,
					textProvider.getText("blazemeter.error" + BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID));
		}
		if (StringUtils.isEmpty(errorUnstable)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE));
		}
		if (StringUtils.isEmpty(errorFail)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL));
		}
		if (StringUtils.isEmpty(respUnstable)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE));
		}
		if (StringUtils.isEmpty(respFail)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL));
		}
		if (StringUtils.isEmpty(testDuration)) {
			errorCollection.addError(BlazeMeterConstants.SETTINGS_TEST_DURATION,
					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_TEST_DURATION));
		}
//		if (StringUtils.isEmpty(mainJMX)) {
//			errorCollection.addError(BlazeMeterConstants.SETTINGS_MAIN_JMX,
//					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_MAIN_JMX));
//		}
//		
//		if (StringUtils.isEmpty(dataFolder)) {
//			errorCollection.addError(BlazeMeterConstants.SETTINGS_DATA_FOLDER,
//					textProvider.getText("blazemeter.error." + BlazeMeterConstants.SETTINGS_DATA_FOLDER));
//		}
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

}

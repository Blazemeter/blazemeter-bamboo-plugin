package com.blazemeter.bamboo.plugin;

public interface Constants {

    //runner properties
	public final static String RUNNER_DESCRIPTION = "BlazeMeter";
	public final static String RUNNER_DISPLAY_NAME = "BlazeMeter";
	public final static String RUNNER_TYPE = "BlazeMeter";
	public final static String BLAZE_METER_STATISTICS_NAME = "BlazeMeterStatistics";
	public final static String USER_KEY = "USER_KEY";
	public final static String BLAZEMETER_URL = "BLAZEMETER_URL";
	public final static String BLAZEMETER_API_VERSION = "api_version";
    public final static String DEFAULT_BZM_SERVER="https://a.blazemeter.com";

	public final static String PROXY_SERVER_NAME = "SERVER_NAME";
	public final static String PROXY_SERVER_PORT = "SERVER_PORT";
	public final static String PROXY_USERNAME = "USERNAME";
	public final static String PROXY_PASSWORD = "PASSWORD";
	
	//settings properties
	public final static String SETTINGS_ALL_TESTS_ID = "all_tests";
	public final static String SETTINGS_ERROR_THRESHOLD_UNSTABLE = "thr_unstable";
	public final static String SETTINGS_ERROR_THRESHOLD_FAIL = "thr_fail";
	public final static String SETTINGS_RESPONSE_TIME_UNSTABLE = "resp_unstable";
	public final static String SETTINGS_RESPONSE_TIME_FAIL = "resp_fail";
	public final static String SETTINGS_TEST_DURATION = "test_duration";
	public final static String SETTINGS_DATA_FOLDER = "data_folder";
	public final static String SETTINGS_MAIN_JMX = "main_jmx";
	public final static String JSON_CONFIGURATION = "json_config";

	//Default properties
	public final static String DEFAULT_SETTINGS_DATA_FOLDER = "DataFolder";
	public final static String BZM_PROPERTIES_FILE="/userKeyFile.properties";
    public final static String NOT_IMPLEMENTED="This call is not implemented.";
    public final static String CREATE_FROM_JSON="create from JSON";
    public final static String NEW_TEST="New test";

    public final static String V2="v2";
    public final static String V3="v3";
}

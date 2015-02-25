package com.blazemeter.bamboo.plugin;

public interface BlazeMeterConstants {
	
    //runner properties
	public final static String USER_KEY = "USER_KEY";
	
	//settings properties
	public final static String SETTINGS_SELECTED_TEST_ID = "selectedtest";
	public final static String SETTINGS_ERROR_THRESHOLD_UNSTABLE = "thr_unstable";
	public final static String SETTINGS_ERROR_THRESHOLD_FAIL = "thr_fail";
	public final static String SETTINGS_RESPONSE_TIME_UNSTABLE = "resp_unstable";
	public final static String SETTINGS_RESPONSE_TIME_FAIL = "resp_fail";
	public final static String SETTINGS_TEST_DURATION = "test_duration";
	public final static String SETTINGS_DATA_FOLDER = "data_folder";
	public final static String SETTINGS_MAIN_JMX = "main_jmx";
	
	//Default properties
	public final static String DEFAULT_SETTINGS_DATA_FOLDER = "DataFolder";
	
	//Report properties
	public final static String REPORT_ERROR_THRESHOLD = "report_thr";
	public final static String REPORT_RESPONSE_TIME = "report_resptime";

    public final static String BLAZEMETER_API_VERSION = "api_version";

    public final static String PROXY_SERVER_NAME = "SERVER_NAME";
    public final static String PROXY_SERVER_PORT = "SERVER_PORT";
    public final static String PROXY_USERNAME = "USERNAME";
    public final static String PROXY_PASSWORD = "PASSWORD";

    public final static String NOT_IMPLEMENTED="This call is not implemented.";

}

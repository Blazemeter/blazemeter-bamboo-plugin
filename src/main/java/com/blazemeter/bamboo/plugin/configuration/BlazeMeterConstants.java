package com.blazemeter.bamboo.plugin.configuration;

public interface BlazeMeterConstants {
	
    public class TestStatus {
        public static final String Running = "Running";
        public static final String NotRunning = "Not Running";
        public static final String NotFound = "NotFound";
        public static final String Error = "error";
    }
	
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
	
}

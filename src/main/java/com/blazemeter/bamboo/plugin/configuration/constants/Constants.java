package com.blazemeter.bamboo.plugin.configuration.constants;

public interface Constants {
    //runner properties
	 String USER_KEY = "USER_KEY";
	
//	 String SETTINGS_API_VERSION = "api_version";

    //settings properties
     String SETTINGS_SELECTED_TEST_ID = "selectedtest";
     String SETTINGS_ERROR_THRESHOLD_UNSTABLE = "thr_unstable";
     String SETTINGS_ERROR_THRESHOLD_FAIL = "thr_fail";
     String SETTINGS_RESPONSE_TIME_UNSTABLE = "resp_unstable";
     String SETTINGS_RESPONSE_TIME_FAIL = "resp_fail";
     String SETTINGS_TEST_DURATION = "test_duration";
     String SETTINGS_DATA_FOLDER = "data_folder";
     String SETTINGS_MAIN_JMX = "main_jmx";

    //Default properties
     String DEFAULT_SETTINGS_DATA_FOLDER = "DataFolder";

    //Report properties
     String REPORT_ERROR_THRESHOLD = "report_thr";
     String REPORT_RESPONSE_TIME = "report_resptime";

     String NOT_IMPLEMENTED="This call is not implemented.";


    //BlazeMeterConfigTask
     String TEST_LIST ="testlist";
//     String API_VERSION_LIST ="apiVersionlist";
     String BLAZEMETER_ERROR ="blazemeter.error.";
}

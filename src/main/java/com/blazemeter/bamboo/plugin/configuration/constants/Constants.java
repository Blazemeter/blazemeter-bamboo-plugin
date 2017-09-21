/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.blazemeter.bamboo.plugin.configuration.constants;

public interface Constants {

    String HTTP_LOG="http-log";
    String BM_TRESHOLDS="bm-thresholds.xml";
    String PLUGIN_KEY="com.blazemeter.bamboo.plugin.BlazeMeterBamboo:blazemeter";

    //settings properties
     String SETTINGS_SELECTED_TEST_ID = "selectedtest";
     String SETTINGS_JTL_REPORT = "jtl.report";
     String SETTINGS_JUNIT_REPORT = "junit.report";
     String SETTINGS_JMETER_PROPERTIES = "jmeter.properties";
     String SETTINGS_NOTES = "notes";
     String SETTINGS_JTL_PATH="jtl.path";
     String SETTINGS_JUNIT_PATH="junit.path";

     String UNKNOWN_TYPE="unknown_type";


    //BlazeMeterConfigTask
     String TEST_LIST ="testlist";
     String REPORT_URL ="reportUrl";
     String BM_KPIS="bm-kpis.jtl";
     String BM_ARTEFACTS="bm-artefacts.zip";


    String PROXY_HOST="http.proxyHost";
    String PROXY_PORT="http.proxyPort";
    String PROXY_USER="http.proxyUser";
    String PROXY_PASS="http.proxyPass";

    String MASTERS="/masters/";
    String PUBLIC_TOKEN="public-token";
    String CHECK_SETTINGS="Check blazemeter & proxy-settings";
    String UNABLE_TO_GET_JTL_ZIP="Unable to get  JTLZIP from ";
    String UNABLE_TO_GET_JUNIT_REPORT="Failed to get junit report for test ";
}

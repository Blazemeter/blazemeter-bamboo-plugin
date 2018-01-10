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
package com.blazemeter.bamboo.plugin.configuration;

public interface Constants {

    String HTTP_LOG ="http-log";
    String PLUGIN_KEY="com.blazemeter.bamboo.plugin.BlazeMeterBamboo:blazemeter";

    //settings properties
     String SETTINGS_SELECTED_TEST_ID = "selectedtest";
     String SETTINGS_JTL_REPORT = "jtl.report";
     String SETTINGS_JUNIT_REPORT = "junit.report";
     String SETTINGS_JMETER_PROPERTIES = "jmeter.properties";
     String SETTINGS_NOTES = "notes";
     String SETTINGS_JTL_PATH="jtl.path";
     String SETTINGS_JUNIT_PATH="junit.path";

    //BlazeMeterConfigTask
     String TEST_LIST ="testlist";
}

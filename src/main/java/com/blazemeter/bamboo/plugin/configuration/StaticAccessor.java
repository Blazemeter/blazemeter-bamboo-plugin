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

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import java.util.HashMap;

public class StaticAccessor {
    private static PluginSettingsFactory settingsFactory;
    private static HashMap<String, String> reportUrls;

    public StaticAccessor(PluginSettingsFactory settingsFactory) {
        StaticAccessor.settingsFactory = settingsFactory;
        StaticAccessor.reportUrls = new HashMap<>();
    }

    public static PluginSettingsFactory getSettingsFactory() {
        return settingsFactory;
    }

    public static HashMap getReportUrls(){return reportUrls;}
}

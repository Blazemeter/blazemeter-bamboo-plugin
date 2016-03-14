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

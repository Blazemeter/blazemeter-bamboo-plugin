package com.blazemeter.bamboo.plugin.configuration;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class StaticAccessor {
	private static PluginSettingsFactory settingsFactory;
	private static PluginSettings settings;

	  public StaticAccessor(PluginSettingsFactory settingsFactory) {
	    StaticAccessor.settingsFactory = settingsFactory;
	  }
	 
	  public static PluginSettingsFactory getSettingsFactory() {
	    return settingsFactory;
	  }

    public static PluginSettings getSettings() {
        return settings;
    }

    public static void setSettings(PluginSettings settings) {
        StaticAccessor.settings = settings;
    }
}

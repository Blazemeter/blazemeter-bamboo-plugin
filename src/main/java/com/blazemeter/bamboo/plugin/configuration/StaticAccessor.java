package com.blazemeter.bamboo.plugin.configuration;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class StaticAccessor {
	private static PluginSettingsFactory settingsFactory;
	 
	  public StaticAccessor(PluginSettingsFactory settingsFactory) {
	    StaticAccessor.settingsFactory = settingsFactory;
	  }
	 
	  public static PluginSettingsFactory getSettingsFactory() {
	    return settingsFactory;
	  }
}

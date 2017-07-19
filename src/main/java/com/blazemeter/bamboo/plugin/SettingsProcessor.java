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
package com.blazemeter.bamboo.plugin;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SettingsProcessor implements PreJobAction {

    PluginSettingsFactory pluginSettingsFactory;

    public SettingsProcessor(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void execute(@NotNull final StageExecution stageExecution, @NotNull final BuildContext buildContext) {
        PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
        String userKey = (String) pluginSettings.get(AdminServlet.Config.class.getName() + AdminServletConst.DOT_USER_KEY);
        String serverUrl = (String) pluginSettings.get(AdminServlet.Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
        buildContext.getBuildDefinition().getTaskDefinitions().get(0).getPluginKey();
        List<TaskDefinition> tds = buildContext.getBuildDefinition().getTaskDefinitions();
        for (TaskDefinition d : tds) {
            if (d.getPluginKey().equals(Constants.PLUGIN_KEY)) {
                Map<String, String> conf = d.getConfiguration();
                conf.put(AdminServlet.Config.class.getName() + AdminServletConst.DOT_USER_KEY, userKey);
                conf.put(AdminServlet.Config.class.getName() + AdminServletConst.DOT_SERVER_URL, serverUrl);
            }
        }
    }
}

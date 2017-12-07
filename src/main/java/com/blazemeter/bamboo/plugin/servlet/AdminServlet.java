/**
 * Copyright 2016 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazemeter.bamboo.plugin.servlet;

import com.atlassian.sal.api.transaction.TransactionCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.bamboo.plugin.configuration.BambooBzmUtils;
import com.blazemeter.bamboo.plugin.configuration.Constants;
import com.blazemeter.bamboo.plugin.logging.*;

public class AdminServlet extends HttpServlet {
    private final TransactionTemplate transactionTemplate;
    private final PluginSettingsFactory pluginSettingsFactory;
    private static final long serialVersionUID = 1L;

    private final TemplateRenderer renderer;

    public AdminServlet(PluginSettingsFactory pluginSettingsFactory, TemplateRenderer renderer,
                        TransactionTemplate transactionTemplate) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.renderer = renderer;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        resp.setContentType("text/html;charset=utf-8");

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String apiId = (String) pluginSettings.get(Constants.API_ID);
        if (apiId != null) {
            context.put(Constants.API_ID, apiId.trim());
            context.put(Constants.API_ID_ERROR, "");
        } else {
            context.put(Constants.API_ID, "");
            context.put(Constants.API_ID_ERROR, "Please set the BlazeMeter api credentials!");
        }

        String apiSecret = (String) pluginSettings.get(Constants.API_SECRET);
        if (apiSecret != null) {
            context.put(Constants.API_SECRET, apiSecret.trim());
            context.put(Constants.API_SECRET_ERROR, "");
        } else {
            context.put(Constants.API_SECRET, "");
            context.put(Constants.API_SECRET_ERROR, "Please set the BlazeMeter api credentials!");
        }

        String url = (String) pluginSettings.get(Constants.URL);
        if (url != null) {
            context.put(Constants.URL, url);
            context.put(Constants.URL_ERROR, "");
        } else {
            context.put(Constants.URL, "");
            context.put(Constants.URL_ERROR, "Please set the BlazeMeter server url!");
        }

        renderer.render(Constants.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
    }

    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        resp.setContentType("text/html;charset=utf-8");

        String apiId = req.getParameter(Constants.API_ID).trim();
        String apiSecret = req.getParameter(Constants.API_SECRET).trim();
        String url = req.getParameter(Constants.URL).trim();

        context.put(Constants.API_ID, apiId);
        context.put(Constants.API_SECRET, apiSecret);
        context.put(Constants.URL, url);

        UserNotifier emptyUserNotifier = new EmptyUserNotifier();
        Logger logger = new BzmLogger();
        BlazeMeterUtils utils = new BambooBzmUtils(apiId, apiSecret, url, url, emptyUserNotifier, logger);

        try {
            User.getUser(utils);
            transactionTemplate.execute(new TransactionCallback() {
                public Object doInTransaction() {
                    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                    pluginSettings.put(Constants.API_ID, apiId);
                    pluginSettings.put(Constants.API_SECRET, apiSecret);
                    pluginSettings.put(Constants.URL, url);
                    return null;
                }
            });
            context.put(Constants.API_ID_ERROR, "User settings are updated. Check that jobs are configured properly");
            context.put(Constants.API_SECRET_ERROR, "User settings are updated. Check that jobs are configured properly");
            context.put(Constants.URL_ERROR, "User settings are updated. Check that jobs are configured properly");
        } catch (Exception e) {
            logger.error("Failed to find user on server.", e);
            context.put(Constants.API_ID_ERROR, "User key is not saved! Check credentials with ID = "
                    + apiId + " and proxy settings.");
            context.put(Constants.API_SECRET_ERROR, "User key is not saved! Check credentials with ID = "
                    + apiId + " and proxy settings.");
            context.put(Constants.URL_ERROR, "Server url is not saved! Check server url "
                    + url + " and proxy settings.");
        } finally {
            renderer.render(Constants.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
        }
    }
}

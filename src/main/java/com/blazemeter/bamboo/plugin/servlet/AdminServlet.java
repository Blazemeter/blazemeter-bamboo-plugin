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
import com.blazemeter.bamboo.plugin.logging.*;
import com.blazemeter.bamboo.plugin.configuration.BambooBzmUtils;

public class AdminServlet extends HttpServlet {
    public static final String API_ID_ERROR = "api_id_error";
    public static final String API_ID_INFO = "api_id_info";
    public static final String API_SECRET_ERROR = "api_secret_error";
    public static final String API_SECRET_INFO = "api_secret_info";
    public static final String URL_ERROR = "url_error";
    public static final String URL_INFO = "url_info";
    public static final String API_ID = "id";
    public static final String API_SECRET = "secret";
    public static final String URL = "url";
    public static final String BLAZEMETER_ADMIN_VM = "blazemeteradmin.vm";
    private final TransactionTemplate transactionTemplate;
    private final PluginSettingsFactory pluginSettingsFactory;
    private static final long serialVersionUID = 1L;
    private final String URL_REGEX = "^https://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

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
        String apiId = (String) pluginSettings.get(API_ID);
        if (apiId != null) {
            context.put(API_ID, apiId.trim());
            context.put(API_ID_ERROR, "");
            context.put(API_ID_INFO, "");
        } else {
            context.put(API_ID, "");
            context.put(API_ID_INFO, "");
            context.put(API_ID_ERROR, "Please set the BlazeMeter api credentials!");
        }

        String apiSecret = (String) pluginSettings.get(API_SECRET);
        if (apiSecret != null) {
            context.put(API_SECRET, apiSecret.trim());
            context.put(API_SECRET_ERROR, "");
            context.put(API_SECRET_INFO, "");
        } else {
            context.put(API_SECRET, "");
            context.put(API_SECRET_ERROR, "Please set the BlazeMeter api credentials!");
            context.put(API_SECRET_INFO, "");
        }

        String url = (String) pluginSettings.get(URL);
        if (url != null) {
            context.put(URL, url);
            context.put(URL_ERROR, "");
            context.put(URL_INFO, "");
        } else {
            context.put(URL, "");
            context.put(URL_ERROR, "Please set the BlazeMeter server url!");
            context.put(URL_INFO, "");
        }

        renderer.render(BLAZEMETER_ADMIN_VM, context, resp.getWriter());
    }

    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        resp.setContentType("text/html;charset=utf-8");

        String apiId = req.getParameter(API_ID).trim();
        String apiSecret = req.getParameter(API_SECRET).trim();
        String url = req.getParameter(URL).trim().replaceAll("/+$", "");
        if (!url.matches(URL_REGEX)) {
            context.put(URL_ERROR, "Server url is not saved! Invalid format!");
            context.put(API_ID, apiId);
            context.put(API_SECRET, apiSecret);
            context.put(URL, url);
            context.put(API_ID_INFO, "");
            context.put(API_SECRET_INFO, "");
            context.put(API_ID_ERROR, "");
            context.put(API_SECRET_ERROR, "");
            context.put(URL_INFO, "");

            renderer.render(BLAZEMETER_ADMIN_VM, context, resp.getWriter());
            return;
        }
        // validate url format
        context.put(API_ID, apiId);
        context.put(API_SECRET, apiSecret);
        context.put(URL, url);

        UserNotifier serverUserNotifier = new ServerUserNotifier();
        Logger logger = new ServerLogger();
        BlazeMeterUtils utils = new BambooBzmUtils(apiId, apiSecret, url, url, serverUserNotifier, logger);

        try {
            User.getUser(utils);
            transactionTemplate.execute(new TransactionCallback() {
                public Object doInTransaction() {
                    PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
                    pluginSettings.put(API_ID, apiId);
                    pluginSettings.put(API_SECRET, apiSecret);
                    pluginSettings.put(URL, url);
                    return null;
                }
            });
            context.put(API_ID_INFO, "User settings are updated. Check that jobs are configured properly");
            context.put(API_ID_ERROR, "");
            context.put(API_SECRET_INFO, "User settings are updated. Check that jobs are configured properly");
            context.put(API_SECRET_ERROR, "");
            context.put(URL_INFO, "User settings are updated. Check that jobs are configured properly");
            context.put(URL_ERROR, "");
        } catch (Exception e) {
            context.put(API_ID_ERROR, "User key is not saved! Check credentials with ID = "
                    + apiId + " and proxy settings.");
            context.put(API_ID_INFO, "");
            context.put(API_SECRET_ERROR, "User key is not saved! Check credentials with ID = "
                    + apiId + " and proxy settings.");
            context.put(API_SECRET_INFO, "");
            context.put(URL_ERROR, "Server url is not saved! Check server url "
                    + url + " and proxy settings.");
            context.put(URL_INFO, "");
        } finally {
            renderer.render(BLAZEMETER_ADMIN_VM, context, resp.getWriter());
        }
    }
}

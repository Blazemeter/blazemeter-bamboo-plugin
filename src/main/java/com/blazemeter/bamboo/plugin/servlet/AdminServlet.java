/**
 * Copyright 2016 BlazeMeter Inc.
 *
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
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.ApiImpl;
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
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import okhttp3.Credentials;

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
        String api_id = (String) pluginSettings.get(AdminServletConst.API_ID);
        if (api_id != null) {
            context.put(AdminServletConst.API_ID, api_id.trim());
            context.put(AdminServletConst.API_ID_ERROR, "");
        } else {
            context.put(AdminServletConst.API_ID, "");
            context.put(AdminServletConst.API_ID_ERROR, "Please set the BlazeMeter api credentials!");
        }

        String api_secret = (String) pluginSettings.get(AdminServletConst.API_SECRET);
        if (api_secret != null) {
            context.put(AdminServletConst.API_SECRET, api_secret.trim());
            context.put(AdminServletConst.API_SECRET_ERROR, "");
        } else {
            context.put(AdminServletConst.API_SECRET, "");
            context.put(AdminServletConst.API_SECRET_ERROR, "Please set the BlazeMeter api credentials!");
        }

        String url = (String) pluginSettings.get(AdminServletConst.URL);
        if (url != null) {
            context.put(AdminServletConst.URL, url);
            context.put(AdminServletConst.URL_ERROR, "");
        } else {
            context.put(AdminServletConst.URL, "");
            context.put(AdminServletConst.URL_ERROR, "Please set the BlazeMeter server url!");
        }

        renderer.render(AdminServletConst.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
    }

    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> context = new HashMap<String, Object>();
        resp.setContentType("text/html;charset=utf-8");

        String api_id = req.getParameter(AdminServletConst.API_ID).trim();
        String api_secret = req.getParameter(AdminServletConst.API_SECRET).trim();
        String url = req.getParameter(AdminServletConst.URL).trim();

        context.put(AdminServletConst.API_ID, api_id);
        context.put(AdminServletConst.API_SECRET, api_secret);
        context.put(AdminServletConst.URL, url);
       String credentials = Credentials.basic(api_id,api_secret);
       Api api= new ApiImpl(credentials, url);
		if (api.verifyCredentials()){
			transactionTemplate.execute(new TransactionCallback() {
				public Object doInTransaction() {
					PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
					pluginSettings.put(AdminServletConst.API_ID, req.getParameter(AdminServletConst.API_ID).trim());
					pluginSettings.put(AdminServletConst.API_SECRET, req.getParameter(AdminServletConst.API_SECRET).trim());
					pluginSettings.put(AdminServletConst.URL, req.getParameter(AdminServletConst.URL).trim());
					return null;
				}
			});
			context.put(AdminServletConst.API_ID_ERROR, "User settings are updated. Check that jobs are configured properly");
			context.put(AdminServletConst.API_SECRET_ERROR, "User settings are updated. Check that jobs are configured properly");
			context.put(AdminServletConst.URL_ERROR, "User settings are updated. Check that jobs are configured properly");
		} else {
			context.put(AdminServletConst.API_ID_ERROR, "User key is not saved! Check credentials with ID = "
                    + req.getParameter(AdminServletConst.API_ID).trim() + " and proxy settings.");
            context.put(AdminServletConst.API_SECRET_ERROR, "User key is not saved! Check credentials with ID = "
                    + req.getParameter(AdminServletConst.API_ID).trim() + " and proxy settings.");
            context.put(AdminServletConst.URL_ERROR, "Server url is not saved! Check server url "
                    + req.getParameter(AdminServletConst.URL).trim() + " and proxy settings.");
        }
        renderer.render(AdminServletConst.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
    }
}

package com.blazemeter.bamboo.plugin.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.blazemeter.bamboo.plugin.ApiVersion;
import com.blazemeter.bamboo.plugin.api.APIFactory;
import com.blazemeter.bamboo.plugin.api.BlazemeterApi;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;

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
		String config = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_USER_KEY);
		String url = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_SERVER_URL);
		String apiVersion = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_API_VERSION);
		if (config != null){
			context.put(AdminServletConst.USER_KEY, config.trim());
			context.put(AdminServletConst.USER_KEY_ERROR, "");
		} else {
			context.put(AdminServletConst.USER_KEY, "");
			context.put(AdminServletConst.USER_KEY_ERROR, "Please set the BlazeMeter user key!");
		}

        if (url != null){
            context.put(AdminServletConst.URL, url);
            context.put(AdminServletConst.URL_ERROR, "");
        } else {
            context.put(AdminServletConst.URL, "");
            context.put(AdminServletConst.URL_ERROR, "Please set the BlazeMeter server url!");
        }

		context.put(AdminServletConst.API_VERSION, apiVersion!=null?apiVersion.trim():ApiVersion.v3.name());
		renderer.render(AdminServletConst.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
	}

	@Override
	protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, Object> context = new HashMap<String, Object>();
		resp.setContentType("text/html;charset=utf-8");
		
		String userKey = req.getParameter(AdminServletConst.USER_KEY).trim();
		String url = req.getParameter(AdminServletConst.URL).trim();
		String apiVersion = req.getParameter(AdminServletConst.API_VERSION).trim();

		context.put(AdminServletConst.USER_KEY, userKey);
		context.put(AdminServletConst.URL, url);
		context.put(AdminServletConst.API_VERSION, apiVersion);

	    /*
        TODO
        Add here auto-detect version
         */
		BlazemeterApi api= APIFactory.getAPI(userKey, url, ApiVersion.v3.name());
		if (api.verifyUserKey()){
			transactionTemplate.execute(new TransactionCallback() {
				public Object doInTransaction() {
					PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();		
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_USER_KEY, req.getParameter(AdminServletConst.USER_KEY).trim());
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_SERVER_URL, req.getParameter(AdminServletConst.URL).trim());
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_API_VERSION, req.getParameter(AdminServletConst.API_VERSION).trim());
					return null;
				}
			});
			
			context.put(AdminServletConst.USER_KEY_ERROR, "");
			context.put(AdminServletConst.URL_ERROR, "");
		} else {
			context.put(AdminServletConst.USER_KEY_ERROR, "User key is not saved! Check that user key "
                    + req.getParameter(AdminServletConst.USER_KEY).trim() + " is valid!");
            context.put(AdminServletConst.URL_ERROR, "Server url is not saved! Check that server url "
                    + req.getParameter(AdminServletConst.URL).trim() + " is valid!");
        }
		renderer.render(AdminServletConst.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
	}

	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static final class Config {
		@XmlElement
		private String userkey;

		public String getUserkey() {
			return userkey;
		}
		public void setUserkey(String userkey) {
			this.userkey = userkey;
		}
	}
}
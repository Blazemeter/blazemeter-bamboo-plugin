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
import com.blazemeter.bamboo.plugin.api.BzmServiceManager;
import com.blazemeter.bamboo.plugin.configuration.constants.AdminServletConst;
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;

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
		String proxyserver = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_PROXY_SERVER);
		String proxyport = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_PROXY_PORT);
		String proxyuser = (String) pluginSettings.get(Config.class.getName() + AdminServletConst.DOT_PROXY_USER);
		String proxypass = (String) pluginSettings.get(Config.class.getName() + Constants.TEST_LIST);
		if (config != null){
			context.put(AdminServletConst.USER_KEY, config.trim());
			context.put(AdminServletConst.USER_KEY_ERROR, "");
		} else {
			context.put(AdminServletConst.USER_KEY, "");
			context.put(AdminServletConst.USER_KEY_ERROR, "Please set the BlazeMeter user key!");
		}

        context.put(AdminServletConst.URL,url.trim());

		if (proxyserver != null){
			context.put(AdminServletConst.PROXY_SERVER, proxyserver.trim());
			context.put(AdminServletConst.PROXY_SERVER_ERROR, "");
		} else {
			context.put(AdminServletConst.PROXY_SERVER, "");
			context.put(AdminServletConst.PROXY_SERVER_ERROR, "");
		}
		
		if (proxyport != null){
			context.put(AdminServletConst.PROXY_PORT, proxyport.trim());
			context.put(AdminServletConst.PROXY_PORT_ERROR, "");
		} else {
			context.put(AdminServletConst.PROXY_PORT, "");
			context.put(AdminServletConst.PROXY_PORT_ERROR, "");
		}
		
		if (proxyuser != null){
			context.put(AdminServletConst.PROXY_USER, proxyuser.trim());
			context.put(AdminServletConst.PROXY_USER_ERROR, "");
		} else {
			context.put(AdminServletConst.PROXY_USER, "");
			context.put(AdminServletConst.PROXY_USER_ERROR, "");
		}
		
		if (proxypass != null){
			context.put(AdminServletConst.PROXY_PASS, proxypass.trim());
			context.put(AdminServletConst.PROXY_PASS_ERROR, "");
		} else {
			context.put(AdminServletConst.PROXY_PASS, "");
			context.put(AdminServletConst.PROXY_PASS_ERROR, "");
		}
		
		renderer.render(AdminServletConst.BLAZEMETER_ADMIN_VM, context, resp.getWriter());
	}

	@Override
	protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, Object> context = new HashMap<String, Object>();
		resp.setContentType("text/html;charset=utf-8");
		
		String userKey = req.getParameter(AdminServletConst.USER_KEY).trim();
		String url = req.getParameter(AdminServletConst.URL).trim();
		String proxyserver = req.getParameter(AdminServletConst.PROXY_SERVER).trim();
		String proxyport = req.getParameter(AdminServletConst.PROXY_PORT).trim();
		String proxyuser = req.getParameter(AdminServletConst.PROXY_USER).trim();
		String proxypass = req.getParameter(AdminServletConst.PROXY_PASS).trim();
		try{
			if ((proxyport == null) || (proxyport == "")){
				proxyport = "-1";
				context.put(AdminServletConst.PROXY_PORT_ERROR, "");
			} else {
				Integer.parseInt(proxyport);
				context.put(AdminServletConst.PROXY_PORT_ERROR, "");
			}
		} catch (Exception e){
			context.put(AdminServletConst.PROXY_PORT_ERROR, "Invalid port value!");
			proxyport = "-1";
		}
		
		context.put(AdminServletConst.USER_KEY, userKey);
		context.put(AdminServletConst.URL, url);
		context.put(AdminServletConst.PROXY_SERVER, proxyserver);
		context.put(AdminServletConst.PROXY_PORT, proxyport == "-1" ? "" : proxyport);
		context.put(AdminServletConst.PROXY_USER, proxyuser);
		context.put(AdminServletConst.PROXY_PASS, proxypass);

		context.put(AdminServletConst.PROXY_SERVER_ERROR, "");
		context.put(AdminServletConst.PROXY_USER_ERROR, "");
		context.put(AdminServletConst.PROXY_PASS_ERROR, "");
		

        /*
        TODO
        Add here auto-detect version
         */
		BzmServiceManager bzmServiceManager =
                BzmServiceManager.getBzmServiceManager(proxyserver, proxyport, proxyuser, proxypass,"v3");
		if (bzmServiceManager.verifyUserKey(userKey)){
		
			transactionTemplate.execute(new TransactionCallback() {
				public Object doInTransaction() {
					PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();		
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_USER_KEY, req.getParameter(AdminServletConst.USER_KEY).trim());
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_SERVER_URL, req.getParameter(AdminServletConst.URL).trim());
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_PROXY_SERVER, req.getParameter(AdminServletConst.PROXY_SERVER).trim());
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_PROXY_PORT, req.getParameter(AdminServletConst.PROXY_PORT).trim());
					pluginSettings.put(Config.class.getName() + AdminServletConst.DOT_PROXY_USER, req.getParameter(AdminServletConst.PROXY_USER).trim());
					pluginSettings.put(Config.class.getName() + Constants.TEST_LIST, req.getParameter(AdminServletConst.PROXY_PASS).trim());
					return null;
				}
			});
			
			context.put(AdminServletConst.USER_KEY_ERROR, "");
		} else {
			context.put(AdminServletConst.USER_KEY_ERROR, "Error! User key not saved! The user key " + req.getParameter(AdminServletConst.USER_KEY).trim() + " is invalid!");
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
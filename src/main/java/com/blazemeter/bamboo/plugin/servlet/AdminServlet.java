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
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.blazemeter.bamboo.plugin.api.BlazeBean;

import com.blazemeter.bamboo.plugin.api.BlazeBean;

public class AdminServlet extends HttpServlet {
	private final TransactionTemplate transactionTemplate;
	private final PluginSettingsFactory pluginSettingsFactory;

	/**
	 * 
	 */
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
		String config = (String) pluginSettings.get(Config.class.getName() + ".userkey");
		String proxyserver = (String) pluginSettings.get(Config.class.getName() + ".proxyserver");
		String proxyport = (String) pluginSettings.get(Config.class.getName() + ".proxyport");
		String proxyuser = (String) pluginSettings.get(Config.class.getName() + ".proxyuser");
		String proxypass = (String) pluginSettings.get(Config.class.getName() + ".proxypass");
		if (config != null){
			context.put("userkey", config.trim());
			context.put("userkey_error", "");
		} else {
			context.put("userkey", "");
			context.put("userkey_error", "Please set the BlazeMeter user key!");
		}

		if (proxyserver != null){
			context.put("proxyserver", proxyserver.trim());
			context.put("proxyserver_error", "");
		} else {
			context.put("proxyserver", "");
			context.put("proxyserver_error", "");
		}
		
		if (proxyport != null){
			context.put("proxyport", proxyport.trim());
			context.put("proxyport_error", "");
		} else {
			context.put("proxyport", "");
			context.put("proxyport_error", "");
		}
		
		if (proxyuser != null){
			context.put("proxyuser", proxyuser.trim());
			context.put("proxyuser_error", "");
		} else {
			context.put("proxyuser", "");
			context.put("proxyuser_error", "");
		}
		
		if (proxypass != null){
			context.put("proxypass", proxypass.trim());
			context.put("proxypass_error", "");
		} else {
			context.put("proxypass", "");
			context.put("proxypass_error", "");
		}
		
		renderer.render("blazemeteradmin.vm", context, resp.getWriter());
	}

	@Override
	protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, Object> context = new HashMap<String, Object>();
		resp.setContentType("text/html;charset=utf-8");
		
		String userKey = req.getParameter("userkey").trim();
		String proxyserver = req.getParameter("proxyserver").trim();
		String proxyport = req.getParameter("proxyport").trim();
		String proxyuser = req.getParameter("proxyuser").trim();
		String proxypass = req.getParameter("proxypass").trim();
		
		try{
			if ((proxyport == null) || (proxyport == "")){
				proxyport = "-1";
				context.put("proxyport_error", "");
			} else {
				Integer.parseInt(proxyport);
				context.put("proxyport_error", "");
			}
		} catch (Exception e){
			context.put("proxyport_error", "Invalid port value!");
			proxyport = "-1";
		}
		
		context.put("userkey", req.getParameter("userkey").trim());
		context.put("proxyserver", proxyserver);
		context.put("proxyport", proxyport == "-1" ? "" : proxyport);
		context.put("proxyuser", proxyuser);
		context.put("proxypass", proxypass);

		context.put("proxyserver_error", "");
		context.put("proxyuser_error", "");
		context.put("proxypass_error", "");

		BlazeBean blazeBean = new BlazeBean(userKey, proxyserver, Integer.parseInt(proxyport), proxyuser, proxypass);
		if (blazeBean.verifyUserKey(userKey)){
		
			transactionTemplate.execute(new TransactionCallback() {
				public Object doInTransaction() {
					PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();		
					pluginSettings.put(Config.class.getName() + ".userkey", req.getParameter("userkey").trim());
					pluginSettings.put(Config.class.getName() + ".proxyserver", req.getParameter("proxyserver").trim());
					pluginSettings.put(Config.class.getName() + ".proxyport", req.getParameter("proxyport").trim());
					pluginSettings.put(Config.class.getName() + ".proxyuser", req.getParameter("proxyuser").trim());
					pluginSettings.put(Config.class.getName() + ".proxypass", req.getParameter("proxypass").trim());
					return null;
				}
			});
			
			context.put("userkey_error", "");
		} else {
			context.put("userkey_error", "Error! User key not saved! The user key " + req.getParameter("userkey").trim() + " is invalid!");
		}
				
		renderer.render("blazemeteradmin.vm", context, resp.getWriter());
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

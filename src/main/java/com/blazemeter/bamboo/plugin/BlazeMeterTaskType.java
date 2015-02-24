package com.blazemeter.bamboo.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.api.BlazeBean;
import com.blazemeter.bamboo.plugin.api.TestInfo;
import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;

public class BlazeMeterTaskType implements TaskType{
	private static final int CHECK_INTERVAL = 60000;
	
	private BlazeBean blazeBean;
	
	private String testId;
	private int testDuration;
	int errorUnstableThreshold;
	int errorFailedThreshold;
	int responseTimeUnstableThreshold;
	int responseTimeFailedThreshold;
	String dataFolder;
	String mainJMX;
	
	boolean needTestUpload;
	File rootDirectory;

	ProcessService processService;
	private final PluginSettingsFactory pluginSettingsFactory;
	
	public BlazeMeterTaskType(final ProcessService processService, PluginSettingsFactory pluginSettingsFactory){
		this.processService = processService;
		this.pluginSettingsFactory = pluginSettingsFactory;
		
		blazeBean = new BlazeBean();
	}
	
	private void addError(String error, BuildLogger logger, CurrentBuildResult currentBuildResult){
		logger.addErrorLogEntry(error);
		currentBuildResult.addBuildErrors(Arrays.asList(error));
	}
	
	@Override
	public TaskResult execute(TaskContext context) throws TaskException {
		final BuildLogger logger = context.getBuildLogger();
		final CurrentBuildResult currentBuildResult = context.getBuildContext().getBuildResult();
		TaskResultBuilder resultBuilder = TaskResultBuilder.create(context);
		
		logger.addBuildLogEntry("BlazeMeter execute task");
		
		PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();	
		String config = (String) pluginSettings.get(Config.class.getName() + ".userkey");
		String proxyserver = (String) pluginSettings.get(Config.class.getName() + ".proxyserver");
		String proxyport = (String) pluginSettings.get(Config.class.getName() + ".proxyport");
		String proxyuser = (String) pluginSettings.get(Config.class.getName() + ".proxyuser");
		String proxypass = (String) pluginSettings.get(Config.class.getName() + ".proxypass");		
		if (proxyserver != null){
			blazeBean.setProxyserver(proxyserver);
		} 
		if (proxyport != null){
			blazeBean.setProxyport(proxyport);
		} 
		if (proxyuser != null){
			blazeBean.setProxyuser(proxyuser);
		} 
		if (proxypass != null){
			blazeBean.setProxypass(proxypass);
		} 		
		if (config != null){
			blazeBean.setUserKey(config);
		} else {
			addError("BlazeMeter user key not defined!", logger, currentBuildResult);
			return resultBuilder.failed().build();
		}
		
		rootDirectory = context.getRootDirectory();
		
		ConfigurationMap configMap = context.getConfigurationMap();
		
		String validation = initialize(configMap, logger, currentBuildResult);
		
		if (validation != null){
			return resultBuilder.failedWithError().build();
		}
		
		logger.addBuildLogEntry("Attempting to start test with id:"+testId);
		boolean started = blazeBean.startTest(testId, logger, currentBuildResult);
		
		if (!started){
			return resultBuilder.failed().build();
		} else {
			if (blazeBean.getSession() != null){//save the session id to the build custom data map
				context.getBuildContext().getBuildResult().getCustomBuildData().put("session_id", blazeBean.getSession());
			} else {
				addError("Failed to retrieve test session id! Report will not be available for this test!", logger, currentBuildResult);
			}
			logger.addBuildLogEntry("Test started. Waiting " + testDuration + " minutes to finish!");
		}
		
		long totalWaitTime = (testDuration+2) * 60 * 1000;//the duration is in minutes so we multiply to get the value in ms
		long nrOfCheckInterval = totalWaitTime / CHECK_INTERVAL;//
		long currentCheck = 0;
		
		TestInfo testInfo;
		while (currentCheck++ < nrOfCheckInterval){
			try {
				Thread.currentThread().sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
				addError("BlazeMeter Interrupted Exception: "+e.getMessage(), logger, currentBuildResult);
				break;
			}
			
			logger.addBuildLogEntry("Check if the test is still running. Time passed since start:"+((currentCheck*CHECK_INTERVAL)/1000/60) + " minutes.");
			testInfo = blazeBean.getTestStatus(testId);
			if (testInfo.getStatus().equals(BlazeMeterConstants.TestStatus.NotRunning)){
				logger.addBuildLogEntry("Test is finished earlier then estimated! Time passed since start:"+((currentCheck*CHECK_INTERVAL)/1000/60) + " minutes.");
				break;
			} 
			else
			if (testInfo.getStatus().equals(BlazeMeterConstants.TestStatus.NotFound)){
				addError("BlazeMeter test not found!", logger, currentBuildResult);
				return resultBuilder.failed().build();
			}
		}
		
		//BlazeMeter test stopped due to user test duration setup reached
		logger.addBuildLogEntry("Stopping test...");
		blazeBean.stopTest(testId, logger, currentBuildResult);
		
		logger.addBuildLogEntry("Test finished. Checking for test report...");
		
        //get testGetArchive information
        boolean waitForReport = blazeBean.waitForReport(logger, currentBuildResult);
        
        if (waitForReport){
        	int reportStatus = blazeBean.getReport(errorFailedThreshold, errorUnstableThreshold, responseTimeFailedThreshold, responseTimeUnstableThreshold, logger, currentBuildResult);

//        	if (reportStatus != -1){
        		//TODO
            	blazeBean.publishReportArtifact(context.getBuildContext().getBuildResult());
//        	}
        	
        	switch (reportStatus) {
        		case -1:
        			return resultBuilder.failed().build();
        		case 0:
        			return resultBuilder.success().build();
        		case 1:
        			return resultBuilder.success().build();
        		default: 
        			return resultBuilder.success().build();
        	}
        } else {
        	return resultBuilder.success().build();
        }

	}
	
	public String initialize(ConfigurationMap configMap, BuildLogger logger, CurrentBuildResult currentBuildResult){
		logger.addBuildLogEntry("Parameter validation");
		
		String validationError = validateParams(configMap, logger);
		if (validationError != null){
			addError(validationError, logger, currentBuildResult);
		} else {
			logger.addBuildLogEntry("Validation passed.");
		}
		
		if (needTestUpload){
			uploadDataFolderFiles(logger, currentBuildResult);
		}

		return validationError;
	}
	
	private String validateParams(Map<String, String> params, BuildLogger logger) {
		
		if (!blazeBean.verifyUserKey(blazeBean.getUserKey())){
			return "Invalid user key defined! Set a valid user key in BlazeMeter Administration page.";
		}
		
		testId = params.get(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID);
		if (isNullorEmpty(testId)) {
			return "No test was defined in the configuration page.";
		} else {
			//verify if the test still exists on BlazeMeter server
			HashMap<String, String> tests = blazeBean.getTests();
			if (tests != null){
				if (!tests.keySet().contains(testId)) {
					return "Test removed from BlazeMeter server.";
				}
			} else {
				return "No tests defined on BlazeMeter server!";
			}
		}
		String testDrt = params.get(BlazeMeterConstants.SETTINGS_TEST_DURATION);
		if (isNullorEmpty(testDrt)) {
			return "Test duration not set.";
		} else {
			try{
				testDuration = Integer.valueOf(testDrt);
			} catch (NumberFormatException nfe){
				return "Test duration is not a number.";
			}
		}

		String errorUnstable = params.get(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		String errorFail = params.get(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL);
		String timeUnstable = params.get(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		String timeFail = params.get(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL);

		try{
			errorFailedThreshold = errorFail.isEmpty()?-1:Integer.valueOf(errorFail);
		} catch (NumberFormatException nfe){
			return "Error threshold failed is not a number.";
		}
		try{
			errorUnstableThreshold = errorUnstable.isEmpty()?-1:Integer.valueOf(errorUnstable);
		} catch (NumberFormatException nfe){
			return "Error threshold unstable is not a number.";
		}
		try{
			responseTimeFailedThreshold = timeFail.isEmpty()?-1:Integer.valueOf(timeFail);
		} catch (NumberFormatException nfe){
			return "Response time failed is not a number.";
		}
		try{
			responseTimeUnstableThreshold = timeUnstable.isEmpty()?-1:Integer.valueOf(timeUnstable);
		} catch (NumberFormatException nfe){
			return "Response time unstable is not a number.";
		}
		
		dataFolder = params.get(BlazeMeterConstants.SETTINGS_DATA_FOLDER);
		if (isNullorEmpty(dataFolder)){
			dataFolder = "";
		}
		
		dataFolder = dataFolder.trim();
		mainJMX = params.get(BlazeMeterConstants.SETTINGS_MAIN_JMX);
		
		logger.addBuildLogEntry("File separator should be " + File.separator);

		if (isNullorEmpty(mainJMX)) {
			needTestUpload = false;
		} else {
			String agentCheckoutDir = rootDirectory.getAbsolutePath();
			if (!((agentCheckoutDir.endsWith("/") || agentCheckoutDir.endsWith("\\")))){//if the path doesn't have folder separator
				agentCheckoutDir += File.separator;//make sure that the path ends with '/'
			}
									
	        if (!isFullPath(dataFolder)){//full path
	        	dataFolder = agentCheckoutDir + dataFolder;
	        } 
	        
	        File folder = new File(dataFolder);

	        if (!folder.exists() || !folder.isDirectory()){
	            return dataFolder + " could not be found on local file system, please check that the folder exists.";
	        }
			needTestUpload = true;
		}
		
		return null;
	}
	
	/**
	 * Upload main JMX file and all the files from the data folder
	 */
    private void uploadDataFolderFiles(BuildLogger logger, CurrentBuildResult currentBuildResult) {
    	logger.addBuildLogEntry("Uploading data files");
        if (dataFolder == null || dataFolder.isEmpty()){
        	addError("Empty data folder. Please enter the path to your data folder or '.' for main folder where the files are checked out.", logger, currentBuildResult);
            return;
        }
        
        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()){
            addError("dataFolder " + dataFolder + " could not be found on local file system, please check that the folder exists.", logger, currentBuildResult);
            return ;
        } else {
        	logger.addBuildLogEntry("DataFolder "+dataFolder+" exists.");
        }

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String file;
            if (listOfFiles[i].isFile()) {
                file = listOfFiles[i].getName();
                if (file.endsWith(mainJMX)){
                	logger.addBuildLogEntry("Uploading main JMX "+mainJMX);
                    blazeBean.uploadJMX(testId, mainJMX, dataFolder + File.separator + mainJMX);
                }
                else {
                	logger.addBuildLogEntry("Uploading data files "+file);
                	blazeBean.uploadFile(testId, dataFolder, file, logger, currentBuildResult);
                }
            }
        }
    }

	
	private boolean isFullPath(String path){
		if (path.startsWith("/")){
			return true;
		}
		
		if (path.length() < 3) {
			return false;
		}
		if (path.substring(0,1).matches("[a-zA-Z]")){//like D:/
			if (path.substring(1, 2).equals(":") && (path.substring(2, 3).equals("/") || path.substring(2, 3).equals("\\"))){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the value is null or empty
	 * @param value
	 * @return true if the value is null or empty, false otherwise
	 */
	private boolean isNullorEmpty(String value){
		if ((value == null) || ("".equals(value))){
			return true;
		}
		return false;
	}

}

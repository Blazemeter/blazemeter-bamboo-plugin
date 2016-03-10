package com.blazemeter.bamboo.plugin;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by zmicer on 21.2.15.
 */
public class Utils {

    private Utils() {
    }

    public static String getReportUrl(Api api, String masterId, BuildLogger logger) {
        JSONObject jo=null;
        String publicToken="";
        String reportUrl=null;
        try {
            jo = api.publicToken(masterId);
            if(jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject(JsonConstants.RESULT);
                publicToken=result.getString("publicToken");
                reportUrl=api.url()+"/app/?public-token="+publicToken+"#masters/"+masterId+"/summary";
            }else{
                logger.addErrorLogEntry("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                logger.addErrorLogEntry("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                reportUrl=api.url()+"/app/#masters/"+masterId+"/summary";
            }

        } catch (Exception e){
            logger.addErrorLogEntry("Problems with generating public-token for report URL");
            logger.addErrorLogEntry("Problems with generating public-token for report URL",e);
        }finally {
            return reportUrl;
        }
    }

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(Utils.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty("version", "N/A");
        }
        return props.getProperty("version");
    }
}

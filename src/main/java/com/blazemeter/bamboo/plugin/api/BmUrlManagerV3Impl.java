package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by zmicer on 21.2.15.
 */
public class BmUrlManagerV3Impl implements BmUrlManager{

    private String SERVER_URL = "https://a.blazemeter.com/";

    public BmUrlManagerV3Impl(String blazeMeterUrl) {
        SERVER_URL = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    public String testStatus(String appKey, String userKey, String sessionId) {
        String testStatus=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            sessionId = URLEncoder.encode(sessionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStatus=SERVER_URL+"/api/latest/sessions/"+sessionId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return testStatus;
    }

    @Override
    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
        return BlazeMeterConstants.NOT_IMPLEMENTED;
    }

    @Override
    public String fileUpload(String appKey, String userKey, String testId, String fileName) {
        return BlazeMeterConstants.NOT_IMPLEMENTED;
    }


    @Override
    public String testStart(String appKey, String userKey, String testId) {
        String testStart=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStart=SERVER_URL+"/api/latest/tests/"
                +testId+"/start?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return testStart;
    }

    @Override
    public String testStop(String appKey, String userKey, String testId) {
        String testStop=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStop=SERVER_URL+"/api/latest/tests/"
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return testStop;
    }

    @Override
    public String testReport(String appKey, String userKey, String reportId) {
        String testAggregateReport=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            reportId = URLEncoder.encode(reportId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testAggregateReport=SERVER_URL+"/api/latest/sessions/"
                +reportId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testAggregateReport;
    }

    @Override
    public String getUrlForTestList(String appKey, String userKey) {
        return BlazeMeterConstants.NOT_IMPLEMENTED;
    }
}

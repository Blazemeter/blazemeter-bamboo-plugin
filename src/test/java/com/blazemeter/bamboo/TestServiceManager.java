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

package com.blazemeter.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskState;
import com.blazemeter.bamboo.plugin.ServiceManager;
import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.ApiV3Impl;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
public class TestServiceManager {
    BuildLogger log= Mockito.mock(BuildLogger.class);

    @BeforeClass
    public static void setUp()throws IOException{
        MockedAPI.startAPI();
        MockedAPI.getTests();
        MockedAPI.startTest();
        MockedAPI.userProfile();
        MockedAPI.stopTestSession();
        MockedAPI.getMasterStatus();
        MockedAPI.getCIStatus();
        MockedAPI.autoDetectVersion();
        MockedAPI.getReportUrl();
        MockedAPI.getTestConfig();
        MockedAPI.putTestInfo();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }


    @Test
    public void getTests(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String,String> tests=ServiceManager.getTests(api);
        Assert.assertTrue(tests.size()==5);
    }

    @Test
    public void startTest(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        String testId=ServiceManager.startTest(api,TestConstants.TEST_MASTER_ID,log);
        Assert.assertEquals("15102806",testId);
    }

    @Test
    public void getReportUrl(){
        String expectedReportUrl=TestConstants.mockedApiUrl+"/app/?public-token=ohImO6c8xstG4qBFqgRnsMSAluCBambtrqsTvAEYEXItmrCfgO#masters/testMasterId/summary";
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        String actReportUrl=ServiceManager.getReportUrl(api, TestConstants.TEST_MASTER_ID,log);
        Assert.assertEquals(expectedReportUrl,actReportUrl);
    }

    @Test
    public void getCIStatus_success(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        TaskState state=ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_SUCCESS, log);
        Assert.assertEquals(state,TaskState.SUCCESS);
    }

    @Test
    public void getCIStatus_failure(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        TaskState taskState=ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_FAILURE, log);
        Assert.assertEquals(taskState,TaskState.FAILED);
    }

    @Test
    public void getCIStatus_error(){
        Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        TaskState state=ServiceManager.ciStatus(api, TestConstants.TEST_MASTER_ERROR, log);
        Assert.assertEquals(state,TaskState.ERROR);
    }

    @Test
    public void stopTestMaster(){
            Api api = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
            boolean terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_25, log);
            Assert.assertEquals(terminate, true);
            terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_70, log);
            Assert.assertEquals(terminate, true);
            terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_100, log);
            Assert.assertEquals(terminate, false);
            terminate = ServiceManager.stopTestMaster(api, TestConstants.TEST_MASTER_140, log);
            Assert.assertEquals(terminate, false);
    }

    @Test
    public void getVersion() throws IOException,JSONException {
        String version= ServiceManager.getVersion();
        Assert.assertTrue(version.matches("^(\\d{1,}\\.+\\d{1,2}\\S*)$"));
    }

}

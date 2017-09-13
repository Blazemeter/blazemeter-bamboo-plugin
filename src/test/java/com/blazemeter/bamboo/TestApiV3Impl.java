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

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.api.*;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import java.util.HashMap;
import okhttp3.Credentials;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import java.io.IOException;

public class TestApiV3Impl {
    private ApiImpl blazemeterApiV3 = null;


    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.getMasterStatus();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.accountId();
        MockedAPI.workspaces();
    }

    @AfterClass
    public static void tearDown(){
        MockedAPI.stopAPI();
    }



    @Test
    public void getTestStatus_Running() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c,TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c,TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c,TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }





    @Test
    public void startTest_multi() throws JSONException,IOException {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c,TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConstants.ID),
                "15105877");
    }

    @Test
    public void getTestReport() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        JSONObject testReport = blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);


    }

    @Test
    public void getTestSessionStatusCode_25() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }


    @Test
    public void getTestSessionStatusCode_70() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }


    @Test
    public void getTestSessionStatusCode_140() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }


    @Test
    public void getTestSessionStatusCode_100() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }


    @Test
    public void getTestSessionStatusCode_0() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_EXCEPTION,TestConstants.TEST_API_SECRET_EXCEPTION);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void accountId() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        Assert.assertTrue(blazemeterApiV3.accounts().size()==3);
    }

    @Test
    public void workspaces() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiImpl(c, TestConstants.mockedApiUrl);
        HashMap<Integer,String> ws=blazemeterApiV3.workspaces();
        Assert.assertEquals(1,ws.size());
        Assert.assertTrue("DWorkspace".equals(ws.get(32563)));
    }

}

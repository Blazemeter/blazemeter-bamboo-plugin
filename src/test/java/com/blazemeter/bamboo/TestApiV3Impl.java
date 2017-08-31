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
import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.google.common.collect.LinkedHashMultimap;
import okhttp3.Credentials;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

public class TestApiV3Impl {
    private ApiV3Impl blazemeterApiV3 = null;


    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTestConfig();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.active();
    }

    @AfterClass
    public static void tearDown(){
        MockedAPI.stopAPI();
    }



    @Test
    public void getTestStatus_Running() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiV3Impl(c,TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiV3Impl(c,TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiV3Impl(c,TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }





    @Test
    public void startTest_multi() throws JSONException,IOException {
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        blazemeterApiV3 = new ApiV3Impl(c,TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConstants.ID),
                "15105877");
    }

    /*
TODO
    @Test
    public void getTestList_5_5() throws IOException, JSONException, ServletException, MessagingException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_5_TESTS, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = blazemeterApiV3.testsMultiMap();
        Assert.assertTrue(testList.asMap().size() == 5);
        Assert.assertTrue(testList.size() == 5);

    }

    @Test
    public void getTestReport() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        JSONObject testReport = blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);


    }

    @Test
    public void getTestsCount_4() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 4);

    }

    @Test
    public void getTestsCount_1() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_1_TEST, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 1);

    }

    @Test
    public void getTestsCount_0() throws IOException, JSONException, ServletException {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_0_TESTS, TestConstants.mockedApiUrl);
        int count = blazemeterApiV3.getTestCount();
        Assert.assertTrue(count == 0);

    }

    @Test
    public void getTestSessionStatusCode_25() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }

    @Test
    public void getTestSessionStatusCode_70() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestSessionStatusCode_0() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        int status = blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void getTestConfig() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        try {
            JSONObject jo = blazemeterApiV3.testConfig(TestConstants.TEST_MASTER_ID);
            Assert.assertTrue(jo.get(JsonConstants.ERROR).equals(JSONObject.NULL));
            Assert.assertFalse(jo.get(JsonConstants.RESULT).equals(JSONObject.NULL));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void active() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean active = blazemeterApiV3.active("5133848");
        Assert.assertTrue(active);
    }

    @Test
    public void activeNot() {
        blazemeterApiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean active = blazemeterApiV3.active("51338483");
        Assert.assertFalse(active);
    }
*/

}

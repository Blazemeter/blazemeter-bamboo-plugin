package com.blazemeter.bamboo;

import com.blazemeter.bamboo.plugin.TestStatus;
import com.blazemeter.bamboo.plugin.api.ApiV3Impl;
import com.blazemeter.bamboo.plugin.api.TestType;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestApiV3Impl {
    private ApiV3Impl apiV3 = null;


    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.active();
        MockedAPI.ping();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        MockedAPI.stopAPI();
    }



    @Test
    public void getTestInfo_null() {
        apiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.getTestConfig(null), null);
    }

    @Test
    public void getTestStatus_Running() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = apiV3.testStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = apiV3.testStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = apiV3.testStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void getTestInfo_NotFound() {
        apiV3 = new ApiV3Impl("",
                TestConstants.mockedApiUrl);
        TestStatus testStatus = apiV3.testStatus("");
        Assert.assertEquals(testStatus, TestStatus.NotFound);
    }


    @Test
    public void getUser_null() {
        apiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.getUserKey(), null);
    }

    @Test
    public void getTestCount_zero() {
        try {
            apiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
            Assert.assertEquals(apiV3.getTestCount(), 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testReport_null() {
        apiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.testReport(null), null);
    }

    @Test
    public void stopTest_null() {
        apiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
        Assert.assertFalse(apiV3.stopTest(null));
    }

    @Test
    public void startTest_null() throws JSONException {
        apiV3 = new ApiV3Impl(null, null);
        Assert.assertEquals(apiV3.startTest(null, null), null);
    }

    @Test
    public void startTest_http() throws JSONException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.http), "15102806");
    }

    @Test
    public void startTest_jmeter() throws JSONException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.jmeter), "15102806");
    }

    @Test
    public void startTest_followme() throws JSONException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.followme), "15102806");
    }

    @Test
    public void startTest_multi() throws JSONException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.multi), "15105877");
    }

    @Test
    public void getTestRunStatus_notFound() {
        apiV3 = new ApiV3Impl(null, TestConstants.mockedApiUrl);
        Assert.assertEquals(apiV3.testStatus(null), TestStatus.NotFound);
    }

    @Test
    public void getTestList_5_5() throws IOException, JSONException, ServletException, MessagingException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = apiV3.getTestList();
        Assert.assertTrue(testList.asMap().size() == 5);
        Assert.assertTrue(testList.size() == 5);

    }

    @Test
    public void getTestList_6_6() throws IOException, JSONException, ServletException, MessagingException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_6_TESTS, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = apiV3.getTestList();
        Assert.assertTrue(testList.asMap().size() == 6);
        Assert.assertTrue(testList.size() == 6);

    }

    @Test
    public void getTestReport() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        JSONObject testReport = apiV3.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);


    }

    @Test
    public void getTestList_null() throws IOException, JSONException, ServletException, MessagingException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = apiV3.getTestList();
        Assert.assertTrue(testList == null);

    }

    @Test
    public void getTestsCount_5() throws IOException, JSONException, ServletException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        try {
            int count = apiV3.getTestCount();
            Assert.assertTrue(count == 5);
        }catch (Exception e){
            Assert.fail();
        }

    }

    @Test
    public void getTestsCount_1() throws IOException, JSONException, ServletException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_1_TEST, TestConstants.mockedApiUrl);
        int count = apiV3.getTestCount();
        Assert.assertTrue(count == 1);

    }

    @Test
    public void getTestsCount_0() throws IOException, JSONException, ServletException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_0_TESTS, TestConstants.mockedApiUrl);
        int count = apiV3.getTestCount();
        Assert.assertTrue(count == 0);

    }

    @Test
    public void getTestsCount_null() throws IOException, JSONException, ServletException {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_INVALID, TestConstants.mockedApiUrl);
        int count = apiV3.getTestCount();
        Assert.assertTrue(count == 0);

    }

    @Test
    public void getTestSessionStatusCode_25() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = apiV3.masterStatus(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }

    @Test
    public void getTestSessionStatusCode_70() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = apiV3.masterStatus(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = apiV3.masterStatus(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        int status = apiV3.masterStatus(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestSessionStatusCode_0() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        int status = apiV3.masterStatus(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void active() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean active = apiV3.active("5133848");
        Assert.assertTrue(active);
    }

    @Test
    public void activeNot() {
        apiV3 = new ApiV3Impl(TestConstants.MOCKED_USER_KEY_VALID, TestConstants.mockedApiUrl);
        boolean active = apiV3.active("51338483");
        Assert.assertFalse(active);
    }
}

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

import com.blazemeter.bamboo.plugin.api.Api;
import com.blazemeter.bamboo.plugin.api.UrlManager;
import okhttp3.Credentials;
import org.apache.commons.io.FileUtils;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;

import java.io.File;
import java.io.IOException;

import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.unlimited;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockedAPI {
    private static ClientAndServer mockServer;
    private static ClientAndProxy proxy;
    private MockedAPI(){}

    public static void startAPI(){
        mockServer = startClientAndServer(TestConstants.mockedApiPort);
        proxy = startClientAndProxy(Integer.parseInt(TestConstants.proxyPort));
    }

    public static void userProfile() throws IOException{



        File jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_positive.json");
        String userProfile= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(userProfile));


        jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_negative.json");
        userProfile= FileUtils.readFileToString(jsonFile);
/*

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_INVALID)
                        ),
                unlimited()
        )
                .respond(
                        response()..withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(userProfile));


        jsonFile = new File(TestConstants.RESOURCES + "/getUserEmail_jexception.txt");
        userProfile= FileUtils.readFileToString(jsonFile);

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_EXCEPTION)
                        ),
                unlimited()
        )
                .respond(
                        response()..withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(userProfile));

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/user")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_RETRIES)
                        ),
                unlimited()
        )
                .respond(
                        response()..withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(""));
TODO
*/

    }


    public static void getMasterStatus() throws IOException{


        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        File jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_100.json");
        String testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4+"/masters/"+TestConstants.TEST_MASTER_100 +"/status")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION,c),
            unlimited()
        )
            .respond(
                response().withHeader(Api.APP_JSON)
            .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_140.json");
        c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath(UrlManager.V4+"/masters/"+TestConstants.TEST_MASTER_140 +"/status")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION,c),
            unlimited()
        )
            .respond(
                response().withHeader( Api.APP_JSON)
            .withStatusCode(200).withBody(testStatus));


        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_NOT_FOUND +"/status")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
            ,
            unlimited()
        )
            .respond(
                response().withHeader( Api.APP_JSON)
            .withStatusCode(200).withBody(testStatus));

        /*
                  File jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_25.json");
        String testStatus= FileUtils.readFileToString(jsonFile);

          mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_25 +"/status")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),unlimited()
        )
                .respond(
                        response()..withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_70.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_70 +"/status")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                       .withHeader(Api.AUTHORIZATION, c),unlimited()
        )
                .respond(
                        response()..withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(testStatus));

        jsonFile = new File(TestConstants.RESOURCES + "/masterStatus_0.json");
        testStatus= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_0 +"/status")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withQueryStringParameters(
                                new Parameter("api_key", TestConstants.MOCKED_USER_KEY_EXCEPTION)
                        ),
                unlimited()
        )
                .respond(
                        response()..withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(testStatus));



TODO
*/

    }

    public static void stopTestSession() throws IOException{

        File jsonFile = new File(TestConstants.RESOURCES + "/terminateTest.json");
        String terminateTest= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_25 +"/terminate")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION,c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(terminateTest));
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_70 +"/terminate")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION,c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(terminateTest));


        jsonFile = new File(TestConstants.RESOURCES + "/stopTest.json");
        String stopTest= FileUtils.readFileToString(jsonFile);
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_100 + "/stop")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION, c),
            unlimited()
        )
            .respond(
                response().withHeader( Api.APP_JSON)
                    .withStatusCode(200).withBody(stopTest));
        mockServer.when(
            request()
                .withMethod("POST")
                .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_140 + "/stop")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION, c),
            unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(stopTest));

    }

    public static void startTest() throws IOException{

        File jsonFile = new File(TestConstants.RESOURCES + "/startTest.json");
        String startTest= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/v4/tests/"+TestConstants.TEST_MASTER_TUT_GY +"/start")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                    .withHeader(Api.AUTHORIZATION,c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(startTest));

        jsonFile = new File(TestConstants.RESOURCES + "/startCollection.json");
        String startCollection= FileUtils.readFileToString(jsonFile);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath(UrlManager.V4+"/collections/"+TestConstants.TEST_MASTER_ID +"/start")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                    .withHeader(Api.AUTHORIZATION,c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(startCollection));
}

    public static void getTests() throws IOException{
        String expectedPath="/api/web/tests";
        File jsonFile = new File(TestConstants.RESOURCES + "/getTests_10.json");
        String getTests= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getTests));

        jsonFile = new File(TestConstants.RESOURCES + "/getTests_1.json");
        getTests= FileUtils.readFileToString(jsonFile);
        c = Credentials.basic(TestConstants.TEST_API_ID_1_TEST,TestConstants.TEST_API_SECRET_1_TEST);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION,c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getTests));


        jsonFile = new File(TestConstants.RESOURCES + "/getTests_0.json");
        getTests= FileUtils.readFileToString(jsonFile);
        c = Credentials.basic(TestConstants.TEST_API_ID_0_TESTS,TestConstants.TEST_API_SECRET_0_TESTS);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getTests));

        jsonFile = new File(TestConstants.RESOURCES + "/getTests_5.json");
        getTests= FileUtils.readFileToString(jsonFile);
        c = Credentials.basic(TestConstants.TEST_API_ID_6_TESTS,TestConstants.TEST_API_SECRET_6_TESTS);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
            unlimited()
        )
            .respond(
                response().withHeader( Api.APP_JSON)
                    .withStatusCode(200).withBody(getTests));
    }




    public static void getTestReport()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/getTestReport.json");
        String getTestReport= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/"+TestConstants.TEST_MASTER_ID +"/reports/main/summary")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getTestReport));

    }


    public static void getTestConfig()  throws IOException{
        File jsonFile = new File(TestConstants.RESOURCES + "/testConfig.json");
        String getTestConfig= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID,TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/tests/"+TestConstants.TEST_MASTER_ID)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getTestConfig));
}

    public static void putTestInfo() throws IOException {
        File returnFile = new File(TestConstants.RESOURCES + "/updateTestDurationResult.json");
        String returnStr = FileUtils.readFileToString(returnFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
            request()
                .withMethod("PUT")
                .withPath("/api/latest/tests/" + TestConstants.TEST_MASTER_ID)
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION, c),
            unlimited()
        )
            .respond(
                response().withHeader( Api.APP_JSON)
                    .withStatusCode(200).withBody(returnStr));
    }


    public static void getCIStatus()  throws IOException{
        File returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_failure.json");
        String returnStr=FileUtils.readFileToString(returnFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_FAILURE +"/ci-status")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(returnStr));

        returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_success.json");
        returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_SUCCESS +"/ci-status")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(returnStr));


        returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_error_61700.json");
        returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_ERROR_61700 + "/ci-status")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION, c),
            unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(returnStr));

        returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_error_0.json");
        returnStr=FileUtils.readFileToString(returnFile);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_ERROR_0 +"/ci-status")
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(returnStr));

        returnFile=new File(TestConstants.RESOURCES+"/getCIStatus_error_70404.json");
        returnStr = FileUtils.readFileToString(returnFile);

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/api/latest/masters/" + TestConstants.TEST_MASTER_ERROR_70404 + "/ci-status")
                .withHeader(Api.ACCEPT, Api.APP_JSON)
                .withHeader(Api.AUTHORIZATION, c),
            unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(returnStr));

    }


    public static void getReportUrl() throws IOException{
        String expectedPath="/api/latest/masters/"+TestConstants.TEST_MASTER_ID +"/publicToken";
        File jsonFile = new File(TestConstants.RESOURCES + "/getReportUrl_pos.json");
        String getReportUrl= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getReportUrl));

        jsonFile = new File(TestConstants.RESOURCES + "/not_found.json");
        getReportUrl= FileUtils.readFileToString(jsonFile);
        c = Credentials.basic(TestConstants.TEST_API_ID_INVALID, TestConstants.TEST_API_SECRET_INVALID);
        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(getReportUrl));


    }


    public static void active() throws IOException{
        String expectedPath="/api/latest/web/active";
        File jsonFile = new File(TestConstants.RESOURCES + "/active.json");
        String active= FileUtils.readFileToString(jsonFile);
        String c = Credentials.basic(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath(expectedPath)
                        .withHeader(Api.ACCEPT, Api.APP_JSON)
                        .withHeader(Api.AUTHORIZATION, c),
                unlimited()
        )
                .respond(
                        response().withHeader( Api.APP_JSON)
                                .withStatusCode(200).withBody(active));
    }


    public static void stopAPI(){
        mockServer.reset();
        mockServer.stop();
        proxy.stop();
    }
}

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

public interface TestConstants {
    String RESOURCES = System.getProperty("user.dir")+"/src/test/java/com/blazemeter/bamboo/resources";

    String TEST_API_ID_VALID="validId";
    String TEST_API_SECRET_VALID="validSecret";
    String TEST_API_ID_INVALID="invalidId";
    String TEST_API_SECRET_INVALID="invalidSecret";
    String TEST_API_ID_EXCEPTION="exceptionId";
    String TEST_API_SECRET_EXCEPTION="exceptionSecret";
    String TEST_API_ID_0_TESTS="0testsId";
    String TEST_API_SECRET_0_TESTS="0testsSecret";
    String TEST_API_ID_1_TEST="1testId";
    String TEST_API_SECRET_1_TEST="1testSecret";
    String TEST_API_ID_5_TESTS="5testsId";
    String TEST_API_SECRET_5_TESTS="5testsSecret";

    int mockedApiPort=1234;
    String proxyPort="2345";
    String mockedApiUrl="http://127.0.0.1:"+mockedApiPort;

    String TEST_MASTER_ID ="testMasterId";
    String TEST_MASTER_TUT_GY ="5270902";
    String TEST_MASTER_NOT_FOUND ="testMaster-not-found";
    String TEST_MASTER_0 ="testMaster-0";
    String TEST_MASTER_25 ="testMaster-25";
    String TEST_MASTER_70 ="testMaster-70";
    String TEST_MASTER_100 ="testMaster-100";
    String TEST_MASTER_140 ="testMaster-140";
    String TEST_MASTER_SUCCESS ="testMasterSuccess";
    String TEST_MASTER_FAILURE ="testMasterFailure";
    String TEST_MASTER_ERROR_61700 ="testMasterError_61700";
    String TEST_MASTER_ERROR_0 ="testMasterError_0";
    String TEST_MASTER_ERROR_70404 ="testMasterError_70404";
}

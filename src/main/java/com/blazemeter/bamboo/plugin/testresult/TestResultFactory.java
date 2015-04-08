package com.blazemeter.bamboo.plugin.testresult;

import com.blazemeter.bamboo.plugin.ApiVersion;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 13/11/14.
 */
public class TestResultFactory {


    private TestResultFactory() {
    }


    public static TestResult getTestResult(JSONObject json,ApiVersion apiVersion) throws IOException, JSONException {
        TestResult result=null;
        try{
            switch (apiVersion) {
                case v2:
                    if (result == null || result instanceof TestResultV3Impl) {
                        result = new TestResultV2Impl(json);
                    }
                    break;
                case v3:
                    if (result == null || result instanceof TestResultV2Impl) {
                        result = new TestResultV3Impl(json);
                    }
                    break;
            }
        }catch(IOException ioe){
            throw ioe;
        }catch(JSONException je){
            throw je;
        }

        return result;
    }
}

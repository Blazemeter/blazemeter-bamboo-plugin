/**
 * Copyright 2016 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import com.blazemeter.bamboo.plugin.configuration.constants.JsonConstants;
import com.blazemeter.bamboo.plugin.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ApiImpl implements Api {

    private Logger logger = (Logger) LoggerFactory.getLogger("com.blazemeter");

    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPass = null;

    private Proxy proxy = Proxy.NO_PROXY;
    private Authenticator auth = Authenticator.NONE;
    private String credentials;
    private String serverUrl;
    UrlManager urlManager;
    private OkHttpClient okhttp = null;

    public ApiImpl() {
        try {
            proxyHost = System.getProperty(Constants.PROXY_HOST);
            if (!StringUtils.isBlank(this.proxyHost)) {
                logger.info("Using http.proxyHost = " + this.proxyHost);

                try {
                    this.proxyPort = Integer.parseInt(System.getProperty(Constants.PROXY_PORT));
                    logger.info("Using http.proxyPort = " + this.proxyPort);

                } catch (NumberFormatException nfe) {
                    logger.warn("Failed to read http.proxyPort: ", nfe);
                }

                this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort));
                this.proxyUser = System.getProperty(Constants.PROXY_USER);
                logger.info("Using http.proxyUser = " + this.proxyUser);
                this.proxyPass = System.getProperty(Constants.PROXY_PASS);
                logger.info("Using http.proxyPass = " + StringUtils.left(this.proxyPass, 4));
            }
            if (!StringUtils.isBlank(this.proxyUser) && !StringUtils.isBlank(this.proxyPass)) {
                this.auth = new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(proxyUser, proxyPass);
                        if (response.request().header(PROXY_AUTHORIZATION) != null) {
                            return null; // Give up, we've already attempted to authenticate.
                        }

                        return response.request().newBuilder()
                            .header(PROXY_AUTHORIZATION, credential)
                            .build();
                    }
                };
            }
            okhttp = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(this.logger))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .proxyAuthenticator(this.auth).build();
        } catch (Exception ex) {
            this.logger.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }

    }

    public ApiImpl(String credentials, String blazeMeterUrl) {
        this();
        this.credentials = credentials;
        this.serverUrl = blazeMeterUrl;
        this.urlManager = new UrlManagerV3Impl(this.serverUrl);
    }

    public ApiImpl(String credentials, String blazeMeterUrl, HttpLogger httpl) {
        this();
        this.credentials = credentials;
        this.serverUrl = blazeMeterUrl;
        this.urlManager = new UrlManagerV3Impl(this.serverUrl);
        HttpLoggingInterceptor httpLog;
        httpLog = new HttpLoggingInterceptor(httpl);
        httpLog.setLevel(HttpLoggingInterceptor.Level.BODY);
        okhttp = new OkHttpClient.Builder()
            .addInterceptor(new RetryInterceptor(this.logger))
            .addInterceptor(httpLog)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .proxy(this.proxy)
            .proxyAuthenticator(this.auth).build();
    }

    @Override
    public int getTestMasterStatusCode(String id) {
        int statusCode = 0;
        try {
            String url = this.urlManager.masterStatus(APP_KEY, id);
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                .addHeader(AUTHORIZATION, this.credentials).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode = result.getInt("progress");
        } catch (Exception e) {
            this.logger.warn("Error getting master status code: ", e);
        } finally {
            {
                return statusCode;
            }
        }
    }

    @Override
    public TestStatus masterStatus(String id) {
        TestStatus testStatus = null;

        try {
            String url = this.urlManager.masterStatus(APP_KEY, id);
            Request r = new Request.Builder().url(url).get()
                .addHeader(ACCEPT, APP_JSON)
                .addHeader(AUTHORIZATION, this.credentials).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has(JsonConstants.STATUS) && !result.getString(JsonConstants.STATUS).equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    if (result.has(JsonConstants.ERRORS) && !result.get(JsonConstants.ERRORS).equals(JSONObject.NULL)) {
                        this.logger.debug("Error while getting master status: " + result.get(JsonConstants.ERRORS).toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                        this.logger.info("Master with id = " + id + " has status = " + TestStatus.NotRunning.name());
                    }
                }
            }
        } catch (Exception e) {
            this.logger.warn("Error while getting master status ", e);
            testStatus = TestStatus.Error;
        }
        return testStatus;
    }

    @Override
    public synchronized HashMap<String, String> startTest(String testId, boolean collection) throws JSONException,
        IOException {
        String url = "";
        HashMap<String, String> startResp = new HashMap<String, String>();
        if (collection) {
            url = this.urlManager.collectionStart(APP_KEY, testId);
        } else {
            url = this.urlManager.testStart(APP_KEY, testId);
        }
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        if (jo == null) {
            if (this.logger.isDebugEnabled())
                this.logger.debug("Received null json object from server while start operation: will do 5 retries");
            boolean isActive = this.active(testId);
            if (!isActive) {
                int retries = 1;
                while (retries < 6) {
                    try {
                        if (this.logger.isDebugEnabled())
                            this.logger.debug("Trying to repeat start request: " + retries + " retry.");
                        this.logger.debug("Pausing thread for " + 10 * retries + " seconds before doing " + retries + " retry.");
                        Thread.sleep(10000 * retries);
                        jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                        if (jo != null) {
                            break;
                        }
                    } catch (InterruptedException ie) {
                        if (this.logger.isDebugEnabled())
                            this.logger.debug("Start operation was interrupted at pause during " + retries + " retry.");
                    } catch (Exception ex) {
                        if (this.logger.isDebugEnabled())
                            this.logger.debug("Received error from server while starting test: " + retries + " retry.");
                    } finally {
                        retries++;
                    }
                }

            }
        }
        JSONObject result = null;
        try {
            result = (JSONObject) jo.get(JsonConstants.RESULT);
            startResp.put(JsonConstants.ID, String.valueOf(result.get(JsonConstants.ID)));
            startResp.put(JsonConstants.TEST_ID, collection ? String.valueOf(result.get(JsonConstants.TEST_COLLECTION_ID)) :
                String.valueOf(result.get(JsonConstants.TEST_ID)));
            startResp.put(JsonConstants.NAME, result.getString(JsonConstants.NAME));
        } catch (Exception e) {
            startResp.put(JsonConstants.ERROR, jo.get(JsonConstants.ERROR).toString());
        } finally {
            return startResp;
        }
    }

    @Override
    public JSONObject stopTest(String testId) throws IOException, JSONException {
        String url = this.urlManager.masterStop(APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public void terminateTest(String testId) throws IOException {
        String url = this.urlManager.testTerminate(APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
            addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        okhttp.newCall(r).execute();
        return;
    }

    @Override
    public JSONObject testReport(String reportId) {

        String url = this.urlManager.testReport(APP_KEY, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                .addHeader(AUTHORIZATION, this.credentials).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            result = new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConstants.RESULT);
            summary = (JSONObject) result.getJSONArray("summary")
                .get(0);
        } catch (JSONException je) {
            this.logger.warn("Aggregate report(result object): " + result);
            this.logger.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                "is valid/not empty.", je);
        } catch (Exception e) {
            this.logger.warn("Aggregate report(result object): " + result);
            this.logger.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                "is valid/not empty.", e);
        } finally {
            return summary;
        }
    }

    @Override
    public Map<String, Collection<String>> getTestsMultiMap() {
        return this.testsMultiMap().asMap();
    }

    @Override
    public LinkedHashMultimap<String, String> testsMultiMap() {
        LinkedHashMultimap<String, String> testListOrdered = LinkedHashMultimap.create();
        HashMap<Integer,String> ws = this.workspaces();
        logger.info("Getting tests...");
        Set<Integer> wsk = ws.keySet();
        for (Integer k : wsk) {
            String wsn=ws.get(k);
            String url = this.urlManager.tests(APP_KEY, k);
            try {
                Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                    .addHeader(AUTHORIZATION, this.credentials).
                        addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
                JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                JSONArray result = null;
                this.logger.info("Received json: " + jo.toString());
                if (jo.has(JsonConstants.ERROR) && (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) &&
                    (((JSONObject) jo.get(JsonConstants.ERROR)).getInt(JsonConstants.CODE) == 401)) {
                    return testListOrdered;
                }
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONArray) jo.get(JsonConstants.RESULT);
                }
                LinkedHashMultimap<String, String> wst = LinkedHashMultimap.create();
                if (result != null && result.length() > 0) {
                    testListOrdered.put(String.valueOf(k) + "." + "workspace", "========" + wsn + "(" + k + ")========");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject entry = null;
                        try {
                            entry = result.getJSONObject(i);
                        } catch (JSONException e) {
                            this.logger.warn("JSONException while getting tests: " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (entry != null) {
                                id = String.valueOf(entry.get(JsonConstants.ID));
                                name = entry.has(JsonConstants.NAME) ? entry.getString(JsonConstants.NAME).replaceAll("&", "&amp;") : "";
                                String testType = null;
                                try {
                                    testType = entry.getJSONObject(JsonConstants.CONFIGURATION).getString(JsonConstants.TYPE);
                                } catch (Exception e) {
                                    testType = Constants.UNKNOWN_TYPE;
                                }
                                wst.put(id + "." + testType, name + "(" + id + "." + testType + ")");
                            }
                        } catch (JSONException ie) {
                            this.logger.warn("JSONException while getting tests: " + ie);
                        }
                    }
                }
                Comparator c = new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
                        return e1.getValue().compareToIgnoreCase(e2.getValue());
                    }
                };
                wst.entries().stream().sorted(c).
                    forEach(entry -> testListOrdered.put(
                        ((Map.Entry<String, String>) entry).getKey(), ((Map.Entry<String, String>) entry).getValue()));
            } catch (Exception e) {
                this.logger.warn("Exception while getting tests: ", e);
                this.logger.warn("Check connection/proxy settings");
                testListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
            }
        }
        return testListOrdered;
    }

    @Override
    public JSONObject user() throws IOException, JSONException {
        String url = this.urlManager.user(APP_KEY);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException, IOException {
        this.logger.info("Trying to get jtl url for the sessionId = " + sessionId);
        String url = this.urlManager.ciStatus(APP_KEY, sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConstants.RESULT);
        return jo;
    }

    @Override
    public String retrieveJUNITXML(String sessionId) throws IOException {
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        String xmlJunit = okhttp.newCall(r).execute().body().string();
        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) throws IOException, JSONException {
        this.logger.info("Trying to get jtl url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, sessionId);
        this.logger.info("Trying to retrieve jtl json for the sessionId = " + sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jtlzip = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jtlzip;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) throws IOException, JSONException {

        String url = this.urlManager.generatePublicToken(APP_KEY, sessionId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        return jo;
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) throws IOException, JSONException {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, masterId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            JSONArray sessions = jo.getJSONObject(JsonConstants.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConstants.ID));
            }
        } catch (JSONException je) {
            this.logger.info("Failed to get list of sessions from JSONObject " + jo, je);
        } catch (Exception e) {
            this.logger.info("Failed to get list of sessions from JSONObject " + jo, e);
        } finally {
            return sessionsIds;
        }
    }

    @Override
    public boolean active(String testId) {
        boolean isActive = false;
        HashMap<Integer, String> ws = this.workspaces();
        Set<Integer> wsk = ws.keySet();
        for (Integer k : wsk) {
            String url = this.urlManager.activeTests(APP_KEY, k);
            JSONObject jo = null;
            try {
                Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                    .addHeader(AUTHORIZATION, this.credentials).
                        addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
                jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                JSONObject result = null;
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONObject) jo.get(JsonConstants.RESULT);
                    JSONArray tests = (JSONArray) result.get(JsonConstants.TESTS);
                    for (int i = 0; i < tests.length(); i++) {
                        if (String.valueOf(tests.getInt(i)).equals(testId)) {
                            isActive = true;
                            return isActive;
                        }
                    }
                    JSONArray collections = (JSONArray) result.get(JsonConstants.COLLECTIONS);
                    for (int i = 0; i < collections.length(); i++) {
                        if (String.valueOf(collections.getInt(i)).equals(testId)) {
                            isActive = true;
                            return isActive;
                        }
                    }
                }
                return isActive;
            } catch (JSONException je) {
                this.logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, je);
                return false;
            } catch (Exception e) {
                this.logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
                return false;
            }
        }
        return isActive;
    }

    @Override
    public boolean notes(String note, String masterId) throws Exception {
        String noteEsc = StringEscapeUtils.escapeJson("{'" + JsonConstants.NOTE + "':'" + note + "'}");
        String url = this.urlManager.masterId(APP_KEY, masterId);
        JSONObject noteJson = new JSONObject(noteEsc);
        RequestBody body = RequestBody.create(TEXT, noteJson.toString());
        Request r = new Request.Builder().url(url).patch(body).addHeader(AUTHORIZATION, this.credentials).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            if (jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report notes to masterId = " + masterId, e);
        }
        return true;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) throws Exception {
        String url = this.urlManager.properties(APP_KEY, sessionId);
        RequestBody body = RequestBody.create(JSON, properties.toString());
        Request r = new Request.Builder().url(url).post(body).addHeader(AUTHORIZATION, this.credentials).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            if (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report properties to sessionId = " + sessionId, e);
        }
        return true;
    }

    @Override
    public boolean verifyCredentials() {
        logger.info("Verifying userKey...");
        String url = this.urlManager.user(APP_KEY);
        try {
            Request r = new Request.Builder().url(url).get().addHeader(AUTHORIZATION, this.credentials).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            logger.info("Got response: " + jo.toString());
            if (jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Got an exception while verifying credentials: " + e);
            return false;
        }
    }

    @Override
    public String getServerUrl() {
        return this.serverUrl;
    }

    @Override
    public HashMap<Integer,String> accounts() {
        String url = this.urlManager.accounts(APP_KEY);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
            .addHeader(AUTHORIZATION, this.credentials).build();
        JSONObject jo = null;
        JSONArray result = null;
        JSONObject dp = null;
        HashMap<Integer, String> acs = new HashMap<>();
        try {
            jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        } catch (IOException ioe) {
            logger.error("Failed to get accounts: " + ioe);
            return acs;
        }
        try {
            result = jo.getJSONArray(JsonConstants.RESULT);
        } catch (Exception e) {
            logger.error("Failed to get accounts: " + e);
            return acs;
        }
        try {
            for (int i = 0; i < result.length(); i++) {
                JSONObject a = result.getJSONObject(i);
                acs.put(a.getInt(JsonConstants.ID),a.getString(JsonConstants.NAME));
            }
        } catch (Exception e) {
            logger.error("Failed to get accounts: " + e);
            return acs;
        }
        return acs;
    }

    @Override
    public HashMap<Integer, String> workspaces() {
        HashMap<Integer, String> acs = this.accounts();
        HashMap<Integer, String> ws = new HashMap<>();

        Set<Integer> keys = acs.keySet();
        for (Integer key : keys) {
            String url = this.urlManager.workspaces(APP_KEY, key);
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                .addHeader(AUTHORIZATION, this.credentials).build();
            JSONObject jo = null;
            JSONArray result = null;
            try {
                jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            } catch (Exception ioe) {
                logger.error("Failed to get workspaces: " + ioe);
                return ws;
            }
            try {
                result = jo.getJSONArray(JsonConstants.RESULT);
            } catch (Exception e) {
                logger.error("Failed to get workspaces: " + e);
                return ws;
            }
            try {

                for (int i = 0; i < result.length(); i++) {
                    JSONObject s = result.getJSONObject(i);
                    ws.put(s.getInt(JsonConstants.ID), s.getString(JsonConstants.NAME));
                }
            } catch (Exception e) {
                logger.error("Failed to get workspaces: " + e);
                return ws;
            }
        }
        return ws;
    }

    @Override
    public boolean collection(String testId) throws Exception {
        boolean exists = false;
        boolean collection = false;

        LinkedHashMultimap tests = this.testsMultiMap();
        Set<Map.Entry> entries = tests.entries();
        for (Map.Entry e : entries) {
            int point = ((String) e.getKey()).indexOf(".");
            if (point > 0 && testId.contains(((String) e.getKey()).substring(0, point))) {
                collection = (((String) e.getKey()).substring(point + 1)).contains("multi");
                if (((String) e.getKey()).substring(point + 1).contains("workspace")) {
                    throw new Exception("Please, select valid testId instead of workspace header");
                }
                exists = true;
            }
            if (collection) {
                break;
            }
        }
        if (!exists) {
            throw new Exception("Test with test id = " + testId + " is not present on server");
        }
        return collection;
    }

}

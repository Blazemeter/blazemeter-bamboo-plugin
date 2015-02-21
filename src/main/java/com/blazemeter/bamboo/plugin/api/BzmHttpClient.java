package com.blazemeter.bamboo.plugin.api;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by zmicer on 21.2.15.
 */
public class BzmHttpClient {
    PrintStream logger = new PrintStream(System.out);
    DefaultHttpClient httpClient;


    public BzmHttpClient(){}


    public DefaultHttpClient getHttpClient() {
        return httpClient;
    }

    public HttpResponse getResponse(String url, JSONObject data) throws IOException {

        logger.println("Requesting : " + url);
        HttpPost postRequest = new HttpPost(url);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null) {
            postRequest.setEntity(new StringEntity(data.toString()));
        }

        HttpResponse response = null;
        try {
            response = this.httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if ((statusCode >= 300) || (statusCode < 200)) {
                throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
            }
        } catch (Exception e) {
            System.err.format("Wrong response: %s\n", e);
        }
        return response;
    }

    @SuppressWarnings("deprecation")
    public HttpResponse getResponseForFileUpload(String url, File file) throws IOException {

        logger.println("Requesting : " + url);
        HttpPost postRequest = new HttpPost(url);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (file != null) {
            postRequest.setEntity(new FileEntity(file, "text/plain; charset=\"UTF-8\""));
        }

        HttpResponse response = null;
        try {
            response = this.httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if ((statusCode >= 300) || (statusCode < 200)) {
                throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
            }
        } catch (Exception e) {
            System.err.format("Wrong response: %s\n", e);
        }

        return response;
    }

    public void configureProxy(String serverName,int serverPort,
                               String userName, String password){
        // Configure the proxy if necessary
        if (serverName != null && !serverName.isEmpty() && serverPort > 0) {
            if (userName != null && !userName.isEmpty()){
                Credentials cred = new UsernamePasswordCredentials(userName, password);
                this.httpClient.getCredentialsProvider().setCredentials(new AuthScope(serverName, serverPort), cred);
            }
            HttpHost proxyHost = new HttpHost(serverName, serverPort);
            this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
        }
    }


    public JSONObject getJsonForFileUpload(String url, File file) {
        JSONObject jo = null;
        try {
            HttpResponse response = this.getResponseForFileUpload(url, file);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                logger.println(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            logger.println("error decoding Json " + e);
        } catch (JSONException e) {
            logger.println("error decoding Json " + e);
        }
        return jo;
    }


    public JSONObject getJson(String url, JSONObject data) {
        JSONObject jo = null;
        try {
            HttpResponse response = this.getResponse(url, data);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                logger.println(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            logger.println("error decoding Json " + e);
        } catch (JSONException e) {
            logger.println("error decoding Json " + e);
        }
        return jo;
    }


}

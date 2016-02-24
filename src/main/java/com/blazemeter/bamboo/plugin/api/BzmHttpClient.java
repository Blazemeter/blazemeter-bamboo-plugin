package com.blazemeter.bamboo.plugin.api;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public class BzmHttpClient {

    DefaultHttpClient httpClient;


  public BzmHttpClient(){
      this.httpClient=new DefaultHttpClient();
  }

    public HttpResponse getHttpResponse(String url, JSONObject data, Method method) throws Exception {
        HttpResponse response = null;
        HttpRequestBase request = null;

//        logger.message("Requesting : " + url);
        if(data!=null){
//            logger.message("Request data : " + data.toString());
        }
        if (method == Method.GET) {
            request = new HttpGet(url);
        } else if (method == Method.POST) {
            request = new HttpPost(url);
            if (data != null) {
                ((HttpPost) request).setEntity(new StringEntity(data.toString()));
            }
        } else if (method == Method.PUT) {
            request = new HttpPut(url);
            if (data != null) {
                ((HttpPut) request).setEntity(new StringEntity(data.toString()));
            }
        }
        else {
            throw new Exception("Unsupported method: " + method.toString());
        }
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json; charset=UTF-8");

        response = this.httpClient.execute(request);

        HttpPost postRequest = new HttpPost(url);





        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null) {
            postRequest.setEntity(new StringEntity(data.toString()));
        }

//        HttpResponse response = null;
        try {
            response = this.httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if ((statusCode >= 300) || (statusCode < 200)) {
                throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
            }
        }catch (Exception e) {
            System.err.format("Wrong response: %s\n", e);
        }

        return response;
    }


    public HttpResponse getResponseForFileUpload(String url, File file) throws IOException {

//        logger.message("Requesting : " + url);
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


    public String getResponseAsString(String url, JSONObject data, Method method){
        String  str = null;
        try {
            HttpResponse response = getHttpResponse(url, data, method);
            if (response != null) {
                str = EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
         e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public JSONObject getResponseAsJson(String url, JSONObject data, Method method) {
        JSONObject jo = null;
        try {
            HttpResponse response = this.getHttpResponse(url, data, method);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                jo = new JSONObject(output);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jo;
    }


    @SuppressWarnings("deprecation")

    public JSONObject getJsonForFileUpload(String url, File file) {
        JSONObject jo = null;
        try {
            HttpResponse response = this.getResponseForFileUpload(url, file);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }


}

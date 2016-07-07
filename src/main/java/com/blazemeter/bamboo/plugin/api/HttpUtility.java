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

package com.blazemeter.bamboo.plugin.api;

import com.blazemeter.bamboo.plugin.configuration.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class HttpUtility {
    private static final Logger logger = Logger.getLogger(Constants.BZM_HTTP_LOG);
    private boolean useProxy=false;
    private String proxyHost=null;
    private int proxyPort=0;
    private String proxyUser=null;
    private String proxyPass=null;

    private transient CloseableHttpClient httpClient = null;
    private HttpHost proxy=null;

    public HttpUtility() {
        this.httpClient = HttpClients.createDefault();
        useProxy=Boolean.parseBoolean(System.getProperty(Constants.USE_PROXY));
        proxyHost=System.getProperty(Constants.PROXY_HOST);

        try{
            this.proxyPort=Integer.parseInt(System.getProperty(Constants.PROXY_PORT));
        }catch (NumberFormatException nfe){
            logger.debug("Failed to read http.proxyPort: ",nfe);
        }
        if(useProxy&&!org.apache.commons.lang3.StringUtils.isBlank(this.proxyHost)){
            this.proxy=new HttpHost(proxyHost,proxyPort);

            this.proxyUser=System.getProperty(Constants.PROXY_USER);
            this.proxyPass=System.getProperty(Constants.PROXY_PASS);
            if(!org.apache.commons.lang3.StringUtils.isEmpty(this.proxyUser)&&!org.apache.commons.lang3.StringUtils.isEmpty(this.proxyPass)){
                Credentials cr=new UsernamePasswordCredentials(proxyUser, proxyPass);
                AuthScope aus=new AuthScope(proxyHost, proxyPort);
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(aus,cr);
                this.httpClient = HttpClients.custom().setProxy(proxy).setDefaultCredentialsProvider(credsProvider).build();
            }else {
                this.httpClient = HttpClients.custom().setProxy(proxy).build();
            }
        }
    }

    public static void setHttpLog(String httpLog){
        FileAppender fa = new FileAppender();
        fa.setName(Constants.BZM_HTTP_LOG);
        fa.setFile(httpLog);
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.DEBUG);
        fa.activateOptions();
        logger.addAppender(fa);
    }

    public static void resetHttpLog(){
        logger.removeAppender(Constants.BZM_HTTP_LOG);
    }

    public <V> HttpResponse httpResponse(String url, V data, Method method) throws IOException {
        if (StringUtils.isBlank(url)) return null;
            logger.debug("Requesting : " + url.substring(0,url.indexOf("?")+14));
        HttpResponse response = null;
        HttpRequestBase request = null;

        try {
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
            } else if(method == Method.PATCH){
                request = new HttpPatch(url);
                if (data != null) {
                    ((HttpPatch) request).setEntity(new StringEntity(data.toString()));
                }
            }
            else {
                throw new RuntimeException("Unsupported method: " + method.toString());
            }
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json; charset=UTF-8");
            if(proxy!=null){
                RequestConfig conf = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();
                request.setConfig(conf);
            }
            response = this.httpClient.execute(request);


            if (response == null || response.getStatusLine() == null) {
                    logger.debug("Erroneous response (Probably null) for url: \n"+ url);
                response = null;
            }
        } catch (Exception e) {
                logger.debug("Problems with creating and sending request: \n", e);
        }
        return response;
    }


    public <T,V> T response(String url, V data, Method method, Class<T> returnType,Class<V> dataType){
        T returnObj=null;
        JSONObject jo = null;
        String output = null;
        HttpResponse response = null;
        try {
            response = httpResponse(url, data, method);
            if (response != null) {
                output = EntityUtils.toString(response.getEntity());
                    logger.debug("Received object from server: " + output);
                if (output.isEmpty()) {
                    throw new IOException();
                }
                jo = new JSONObject(output);
            }
        } catch (IOException ioe) {
                logger.debug("Received empty response from server: ",ioe);
            return null;
        } catch (JSONException e) {
                logger.debug("ERROR decoding Json: ", e);
            returnType= (Class<T>) String.class;
            return returnType.cast(output);
        }

        try{
            returnObj=returnType.cast(jo);

        }catch (ClassCastException cce){
                logger.debug("Failed to parse response from server: ", cce);
            throw new RuntimeException(jo.toString());

        }
        return returnObj;
    }

}

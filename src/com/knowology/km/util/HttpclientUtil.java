package com.knowology.km.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.List;

/**
 * HttpClient 工具类
 */
public class HttpclientUtil {
    public static String get(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        String result;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)){
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, Consts.UTF_8);
            EntityUtils.consume(entity);
        }
        return result;
    }
    
    public static boolean testGet(String url) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        return response.getStatusLine().getStatusCode() == 200;
    }
    
    public static String post(String url, List<NameValuePair> formParams) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        String result;
        try (CloseableHttpResponse response = httpClient.execute(httpPost)){
            HttpEntity responseEntity = response.getEntity();
            result = EntityUtils.toString(responseEntity, Consts.UTF_8);
            EntityUtils.consume(entity);
        }
        return result;
    }
    public static String post(String url, String json) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        String result;
        try (CloseableHttpResponse response = httpClient.execute(httpPost)){
            HttpEntity responseEntity = response.getEntity();
            result = EntityUtils.toString(responseEntity, Consts.UTF_8);
            EntityUtils.consume(entity);
        }
        return result;
    }
}
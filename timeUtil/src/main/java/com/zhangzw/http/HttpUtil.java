//package com.zhangzw.http;
//
//import com.alibaba.fastjson.JSONObject;
//import io.netty.channel.ConnectTimeoutException;
//import lombok.extern.slf4j.Slf4j;
//import oauth.signpost.OAuthConsumer;
//import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
//import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
//import org.apache.commons.io.IOUtils;
//import org.apache.http.*;
//import org.apache.http.client.HttpRequestRetryHandler;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.*;
//import org.apache.http.client.protocol.HttpClientContext;
//import org.apache.http.client.utils.URIBuilder;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HttpContext;
//
//import javax.net.ssl.SSLException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.SocketTimeoutException;
//import java.net.URI;
//import java.net.UnknownHostException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author march
// */
//@Slf4j
//public class HttpUtil {
//
//    public final static String METHOD_GET = "GET";
//    public final static String METHOD_POST = "POST";
//
//    private final static int SOCKET_TIMEOUT = 60000;
//    private final static int CONNECT_TIMOUT = 60000;
//
//
//    public static CloseableHttpClient getHttpClient() {
//        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(SOCKET_TIMEOUT)
//                .setConnectTimeout(CONNECT_TIMOUT)
//                .build();
//
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setRetryHandler(new HttpRequestRetryHandler() {
//                    @Override
//                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
//                        if (executionCount >= 3) {
//                            // 超过请求次数重试
//                            return false;
//                        }
//                        if (exception instanceof UnknownHostException) {
//                            // Unknown host
//                            return false;
//                        }
//                        if (exception instanceof ConnectTimeoutException) {
//                            // Connection refused
//                            return false;
//                        }
//                        if (exception instanceof SSLException) {
//                            // SSL handshake exception
//                            return false;
//                        }
//                        HttpClientContext clientContext = HttpClientContext.adapt(context);
//                        HttpRequest request = clientContext.getRequest();
//                        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
//                        if (idempotent) {
//                            //  如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
//                            return true;
//                        }
//                        return false;
//                    }
//                })
//                .setConnectionManager(cm)
//                .setDefaultRequestConfig(requestConfig)
//                .build();
//        return httpClient;
//    }
//
//
//    /**
//     * http 请求传参的方法 返回String
//     */
//
//    public static String httpWithParams(String url, String method, Map<String, String> headers, Map<String, Object> formParams) throws Exception {
//        String result = "";
//        // 创建默认的httpClient实例.
//        CloseableHttpClient httpClient = getHttpClient();
//        CloseableHttpResponse response = null;
//        HttpEntityEnclosingRequestBase httpEntity = new HttpEntityEnclosingRequestBase() {
//            @Override
//            public String getMethod() {
//                return method;
//            }
//        };
//        httpEntity.setURI(URI.create(url));
//        try {
//            if (null != headers) {
//                for (Map.Entry<String, String> header : headers.entrySet()) {
//                    httpEntity.setHeader(header.getKey(), header.getValue());
//                }
//            }
//            if (null != formParams) {
//                StringEntity entity = new StringEntity(JSONObject.toJSONString(formParams), ContentType.APPLICATION_JSON);
//                httpEntity.setEntity(entity);
//            }
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setSocketTimeout(SOCKET_TIMEOUT)
//                    .setConnectTimeout(CONNECT_TIMOUT)
//                    .build();
//            httpEntity.setConfig(requestConfig);
//            response = httpClient.execute(httpEntity);
//            if (response != null && response.getStatusLine().getStatusCode() == 200) {
//                result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//                return result;
//            }
//        } catch (SocketTimeoutException e) {
//            // 网络请求超时 抛出异常
//            throw e;
//        } catch (Exception e) {
//            log.error("{}", e.getMessage(), e);
//        } finally {
//            IOUtils.closeQuietly(response);
//        }
//        return null;
//    }
//
//    public static String httpWithObj(String url, String method, Map<String, String> headers, Object formParams) throws Exception {
//        String result = "";
//        // 创建默认的httpClient实例.
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        HttpEntityEnclosingRequestBase httpEntity = new HttpEntityEnclosingRequestBase() {
//            @Override
//            public String getMethod() {
//                return method;
//            }
//        };
//        httpEntity.setURI(URI.create(url));
//        try {
//            if (null != headers) {
//                for (Map.Entry<String, String> header : headers.entrySet()) {
//                    httpEntity.setHeader(header.getKey(), header.getValue());
//                }
//            }
//            if (null != formParams) {
//                httpEntity.setEntity(new StringEntity(JSONObject.toJSONString(formParams), ContentType.APPLICATION_JSON));
//            }
//
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setSocketTimeout(SOCKET_TIMEOUT)
//                    .setConnectTimeout(CONNECT_TIMOUT)
//                    .build();
//            httpEntity.setConfig(requestConfig);
//
//            response = httpClient.execute(httpEntity);
//            if (response != null && response.getStatusLine().getStatusCode() == 200) {
//                result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//                return result;
//            }
//        } catch (Exception e) {
//            log.error("{}", e.getMessage(), e);
//        } finally {
//            IOUtils.closeQuietly(response);
//        }
//        return null;
//    }
//
//    public static String httpDoGetToQtt(String url, Map<String, String> headers, Map<String, Object> formParams) throws Exception {
//        String result = "";
//        // 创建Httpclient对象
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        //response 对象
//        CloseableHttpResponse response = null;
//        // 定义请求的参数
//        URIBuilder uriBuilder = new URIBuilder(url);
//        if (formParams != null) {
//            for (Map.Entry<String, Object> entry : formParams.entrySet()) {
//                uriBuilder.addParameter(entry.getKey(), JSONObject.toJSONString(entry.getValue()));
//            }
//        }
//        URI uri = uriBuilder.build();
//        // 创建http GET请求
//        HttpGet httpGet = new HttpGet(uri);
//        if (headers != null) {
//            for (Map.Entry<String, String> entry : headers.entrySet()) {
//                httpGet.setHeader(entry.getKey(), entry.getValue());
//            }
//        }
//
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(SOCKET_TIMEOUT)
//                .setConnectTimeout(CONNECT_TIMOUT)
//                .build();
//        httpGet.setConfig(requestConfig);
//        try {
//            // 执行http get请求
//            response = httpclient.execute(httpGet);
//            // 判断返回状态是否为200
//            if (response != null && response.getStatusLine().getStatusCode() == 200) {
//                result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//                return result;
//            }
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//            httpclient.close();
//        }
//        return null;
//    }
//
//
//    public static String httpDoPost(String url, Map<String, String> headers, Map<String, Object> formParams) throws Exception {
//        String result = "";
//        // 创建Httpclient对象
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        //response 对象
//        CloseableHttpResponse response = null;
//
//        // 创建http GET请求
//        HttpPost httpPost = new HttpPost(url);
//        if (headers != null) {
//            for (Map.Entry<String, String> entry : headers.entrySet()) {
//                httpPost.setHeader(entry.getKey(), entry.getValue());
//            }
//        }
//        if (formParams != null) {
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//            for (Map.Entry<String, Object> entry : formParams.entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue().toString();
//                nameValuePairs.add(new BasicNameValuePair(key, value));
//            }
//            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
//        }
//
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(SOCKET_TIMEOUT)
//                .setConnectTimeout(CONNECT_TIMOUT)
//                .build();
//        httpPost.setConfig(requestConfig);
//
//        try {
//            // 执行http get请求
//            response = httpclient.execute(httpPost);
//            // 判断返回状态是否为200
//            if (response != null && response.getStatusLine().getStatusCode() == 200) {
//                result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//                return result;
//            }
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//            httpclient.close();
//        }
//        return null;
//    }
//
//
//    public static String httpDoGet(String url, Map<String, String> headers, Map<String, String> formParams) throws Exception {
//        String result = "";
//        // 创建Httpclient对象
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        //response 对象
//        CloseableHttpResponse response = null;
//        // 定义请求的参数
//        URIBuilder uriBuilder = new URIBuilder(url);
//        if (formParams != null) {
//            for (Map.Entry<String, String> entry : formParams.entrySet()) {
//                uriBuilder.addParameter(entry.getKey(), entry.getValue());
//            }
//        }
//        URI uri = uriBuilder.build();
//        // 创建http GET请求
//        HttpGet httpGet = new HttpGet(uri);
//        if (headers != null) {
//            for (Map.Entry<String, String> entry : headers.entrySet()) {
//                httpGet.setHeader(entry.getKey(), entry.getValue());
//            }
//        }
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(SOCKET_TIMEOUT)
//                .setConnectTimeout(CONNECT_TIMOUT)
//                .build();
//        httpGet.setConfig(requestConfig);
//        try {
//            // 执行http get请求
//            response = httpclient.execute(httpGet);
//            // 判断返回状态是否为200
//            if (response != null && response.getStatusLine().getStatusCode() == 200) {
//                result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
//                return result;
//            }
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//            httpclient.close();
//        }
//        return null;
//    }
//
//
//    public static String sendOauthGet(String url, String consumerKey, String consumerSecret) throws Exception {
//
//        HttpRequestBase httpRequest = new HttpGet(new URI(url));
//
//        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
//        oAuthConsumer.setTokenWithSecret(null, null);
//        oAuthConsumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
//        oAuthConsumer.sign(httpRequest);
//
//        try (CloseableHttpClient client = HttpClients.createDefault()) {
//            HttpResponse response = client.execute(httpRequest);
//            InputStream inputStraem = response.getEntity().getContent();
//            String json = IOUtils.toString(inputStraem, StandardCharsets.UTF_8.name());
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                return json;
//            }
//            // 异常
//            String msg = String.format("status:%d, body:%s", response.getStatusLine().getStatusCode(), json);
//            throw new RuntimeException(msg);
//        }
//    }
//}

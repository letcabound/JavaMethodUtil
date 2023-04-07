package com.zhangzw.http;

import com.zhangzw.exception.ParamException;
import okhttp3.*;
import okhttp3.FormBody.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Http/Https 调用工具类
 * <p>
 * SPDY 调用需要添加jvm参数
 * Http/Https 调用工具类 实现同步/异步获取数据
 * @author luya
 * @version 1.0.0
 */
public class HttpUtils {
    private HttpUtils() {
    }

    private static OkHttpClient mOkHttpClient = null;

    /**
     * 超时时间
     */
    @SuppressWarnings("all")
    public static final Long TIME_OUT_SECONDS = 30L;
    @SuppressWarnings("all")
    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    @SuppressWarnings("all")
    public static final String CHARSET_UTF8 = "UTF-8";


    /**
     * 请求超时的设置
     *
     * @param timeOutSeconds 超时时间
     * @param readTimeOut    读超时
     * @param writeTimeOut   写超时
     */
    @SuppressWarnings("all")
    public static void init(Long timeOutSeconds, Long readTimeOut, Long writeTimeOut) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (null != timeOutSeconds && 0L <= timeOutSeconds) {
            builder.connectTimeout(timeOutSeconds, TimeUnit.SECONDS);
        } else {
            builder.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        }

        if (null != readTimeOut && 0L <= readTimeOut) {
            builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        }

        if (null != writeTimeOut && 0L <= writeTimeOut) {
            builder.writeTimeout(writeTimeOut, TimeUnit.SECONDS);
        }
        mOkHttpClient = builder.build();
    }

    /**
     * 初始化httpclient链接
     *
     * @param request 请求
     * @author liuyanping
     */
    private static OkHttpClient getHttpClient(Request request, Proxy proxy) {
        if (null == mOkHttpClient) {
            init(TIME_OUT_SECONDS, null, null);
        }
        OkHttpClient.Builder builder = mOkHttpClient.newBuilder();

        if (!request.isHttps()) {
            return builder.build();
        }

        SSLBuilder sslBuilder = SSLBuilder.builder();
        if(null != proxy){
            builder.proxy(proxy);
        }
        return builder.sslSocketFactory(sslBuilder.getSSLSocketFactory(), sslBuilder.getX509TrustManager())
                .hostnameVerifier(sslBuilder.getNotVerifyHostnameVerifier()).build();
    }

    private static Request getRequest(String url, Headers headers, Map<String, String> paramsMap, String body) {
        Request.Builder builder = new Request.Builder().url(attachParam(url, paramsMap));
        if (headers != null) {
            builder.headers(headers);
        }
        if (StringUtils.isNotBlank(body)) {
            builder.post(RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON), body));
        }
        return builder.build();
    }

    private static Request getPutRequest(String url, Headers headers, Map<String, String> paramsMap, String body) {
        Request.Builder builder = new Request.Builder().url(attachParam(url, paramsMap));
        if (headers != null) {
            builder.headers(headers);
        }
        if (StringUtils.isNotBlank(body)) {
            builder.put(RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON), body));
        }
        return builder.build();
    }

    private static Request getFormRequest(String url, Headers headers, Map<String, String> paramsMap,
                                          Map<String, String> body) {
        Request.Builder builder = new Request.Builder().url(attachParam(url, paramsMap));
        if (headers != null) {
            builder.headers(headers);
        }
        if (null != body && !body.isEmpty()) {
            Builder formBuilder = new Builder();
            Set<Entry<String, String>> entrySet = body.entrySet();
            for (Entry<String, String> entry : entrySet) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
            builder.post(formBuilder.build());
        }
        return builder.build();
    }

    private static Request getPutFormRequest(String url, Headers headers, Map<String, String> paramsMap,
                                             Map<String, String> body) {
        Request.Builder builder = new Request.Builder().url(attachParam(url, paramsMap));
        if (headers != null) {
            builder.headers(headers);
        }
        if (null != body && !body.isEmpty()) {
            Builder formBuilder = new Builder();
            Set<Entry<String, String>> entrySet = body.entrySet();
            for (Entry<String, String> entry : entrySet) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
            builder.put(formBuilder.build());
        }
        return builder.build();
    }

    /**
     * 拼接参数
     *
     * @param url url
     * @param paramsMap 参数
     * @author 137127
     */
    private static String attachParam(String url, Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return url;
        }
        List<BasicNameValuePair> params = new ArrayList<>();
        for (Iterator<String> iterator = paramsMap.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            params.add(new BasicNameValuePair(key, paramsMap.get(key)));
        }
        if (url.endsWith("?")) {
            return url + URLEncodedUtils.format(params, CHARSET_UTF8);
        }
        return url + "?" + URLEncodedUtils.format(params, CHARSET_UTF8);
    }

    /**
     * 同步请求
     *
     * @param request 请求
     */
    @SuppressWarnings("all")
    public static Response execute(Request request,Proxy proxy) {
        try {
            return getHttpClient(request,proxy).newCall(request).execute();
        } catch (IOException e) {
            throw new ParamException("请求报错",e);
        }
    }

    /**
     * 异步请求
     *
     * @param request 请求
     * @param callback 回调
     */
    @SuppressWarnings("all")
    public static void enqueue(Request request, Callback callback,Proxy proxy) {
        getHttpClient(request,proxy).newCall(request).enqueue(callback);
    }

    /**
     * 异步请求Get
     *
     * @param url url
     * @param callback 回调
     * @author 137127
     */
    public static void asyncGet(String url, Callback callback,Proxy proxy) {
        asyncGet(url, null, callback,proxy);
    }
    @SuppressWarnings("all")
    public static void asyncGet(String url, Headers headers, Callback callback,Proxy proxy) {
        asyncGet(url, headers, null, callback,proxy);
    }
    @SuppressWarnings("all")
    public static void asyncGet(String url, Headers headers, Map<String, String> paramsMap, Callback callback,Proxy proxy) {
        enqueue(getRequest(url, headers, paramsMap, null), callback,proxy);
    }

    /**
     * 异步Post
     *
     * @param url url
     * @param callback 回调
     */
    public static void asyncPost(String url, Callback callback,Proxy proxy) {
        asyncPost(url, null, callback,proxy);
    }
    @SuppressWarnings("all")
    public static void asyncPost(String url, Headers headers, Callback callback,Proxy proxy) {
        asyncPost(url, headers, null, callback,proxy);
    }
    @SuppressWarnings("all")
    public static void asyncPost(String url, Headers headers, String body, Callback callback,Proxy proxy) {
        enqueue(getRequest(url, headers, null, body), callback,proxy);
    }

    /**
     * 异步Put
     *
     * @param url url
     * @param callback 回调
     */
    public static void asyncPut(String url, Callback callback,Proxy proxy) {
        asyncPut(url, null, callback,proxy);
    }
    @SuppressWarnings("all")
    public static void asyncPut(String url, Headers headers, Callback callback,Proxy proxy) {
        asyncPut(url, headers, null, callback,proxy);
    }
    @SuppressWarnings("all")
    public static void asyncPut(String url, Headers headers, String body, Callback callback,Proxy proxy) {
        enqueue(getPutRequest(url, headers, null, body), callback,proxy);
    }

    /**
     * 同步Get请求
     *
     * @param url url
     * @author 137127
     */
    public static Response syncGet(String url,Proxy proxy) {
        return syncGet(url, null,proxy);
    }
    @SuppressWarnings("all")
    public static Response syncGet(String url, Headers headers,Proxy proxy) {
        return syncGet(url, headers, null,proxy);
    }
    @SuppressWarnings("all")
    public static Response syncGet(String url, Headers headers, Map<String, String> paramsMap,Proxy proxy) {
        return execute(getRequest(url, headers, paramsMap, null),proxy);
    }
    @SuppressWarnings("all")
    public static String syncGetString(String url,Proxy proxy) {
        return syncGetString(url, null,proxy);
    }
    @SuppressWarnings("all")
    public static String syncGetString(String url, Headers headers,Proxy proxy) {
        return syncGetString(url, headers, null,proxy);
    }
    @SuppressWarnings("all")
    public static String syncGetString(String url, Headers headers, Map<String, String> paramsMap,Proxy proxy) {
        try (Response response = syncGet(url, headers, paramsMap,proxy)) {
            verifySuccess(response.code());
            return response.body().string();
        } catch (IOException e) {
            throw new ParamException("get请求报错",e);
        }
    }

    /**
     * 同步Post请求
     *
     * @param url url
     * @author 137127
     */
    public static Response syncPost(String url,Proxy proxy) {
        return syncPost(url, null,proxy);
    }
    @SuppressWarnings("all")
    public static Response syncPost(String url, Headers headers,Proxy proxy) {
        return syncPost(url, headers, null,proxy);
    }
    @SuppressWarnings("all")
    public static Response syncPost(String url, Headers headers, String body,Proxy proxy) {
        return execute(getRequest(url, headers, null, body),proxy);
    }
    @SuppressWarnings("all")
    public static Response syncPostForm(String url, Headers headers, Map<String, String> body,Proxy proxy) {
        return execute(getFormRequest(url, headers, null, body),proxy);
    }

    public static String syncPostString(String url,Proxy proxy) {
        return syncPostString(url, null,proxy);
    }
    @SuppressWarnings("all")
    public static String syncPostString(String url, Headers headers,Proxy proxy) {
        return syncPostString(url, headers, null,proxy);
    }
    @SuppressWarnings("all")
    public static String syncPostString(String url, Headers headers, String body,Proxy proxy) {
        try (Response response = syncPost(url, headers, body,proxy)) {
            verifySuccess(response.code());
            return response.body().string();
        } catch (IOException e) {
            throw new ParamException("post请求报错",e);
        }
    }

    /**
     * 同步Put请求
     *
     * @param url url
     * @author 137127
     */
    public static Response syncPut(String url,Proxy proxy) {
        return syncPut(url, null,proxy);
    }
    @SuppressWarnings("all")
    public static Response syncPut(String url, Headers headers,Proxy proxy) {
        return syncPut(url, headers, null,proxy);
    }
    @SuppressWarnings("all")
    public static Response syncPut(String url, Headers headers, String body,Proxy proxy) {
        return execute(getPutRequest(url, headers, null, body),proxy);
    }
    @SuppressWarnings("all")
    public static Response syncPutForm(String url, Headers headers, Map<String, String> body,Proxy proxy) {
        return execute(getPutFormRequest(url, headers, null, body),proxy);
    }

    public static String syncPutString(String url,Proxy proxy) {
        return syncPutString(url, null,proxy);
    }
    @SuppressWarnings("all")
    public static String syncPutString(String url, Headers headers,Proxy proxy) {
        return syncPutString(url, headers, null,proxy);
    }
    @SuppressWarnings("all")
    public static String syncPutString(String url, Headers headers, String body,Proxy proxy) {
        try (Response response = syncPut(url, headers, body,proxy)) {
            verifySuccess(response.code());
            return response.body().string();
        } catch (IOException e) {
            throw new ParamException("put请求报错",e);
        }
    }

    public static String syncFormPostString(String url, Headers headers, Map<String, String> body,Proxy proxy) {
        try (Response response = syncPostForm(url, headers, body,proxy)) {
            //此处由于toon返回错误码的同时请求状态码返回302,导致此处直接抛出IOException,调用方无法获得错误码
            //同时联系toon方面,他们会调整此处代码
            //针对上面的问题重新定义了对response code的判断
            verifySuccess(response.code());
            return response.body().string();
        } catch (IOException e) {
            throw new ParamException("form post请求报错",e);
        }
    }

    private static void verifySuccess(int code) {
        if (code < 200 && code > 302) {
            throw new ParamException("接口相应异常");
        }
    }

    /**
     * SSL/TLS处理类
     */
    private static class SSLBuilder {
        private String certFileLocation;
        @SuppressWarnings("all")
        public static SSLBuilder builder() {
            return new SSLBuilder();
        }

        public static SSLBuilder builder(String certFileLocation) {
            SSLBuilder builder = new SSLBuilder();
            builder.certFileLocation = certFileLocation;
            return builder;
        }

        @SuppressWarnings("all")
        public HostnameVerifier getNotVerifyHostnameVerifier() {
            return (hostname, session) -> true;
        }

        @SuppressWarnings("all")
        public X509TrustManager getX509TrustManager() {
            try {
                if (StringUtils.isNotBlank(certFileLocation)) {
                    return trustManagerForCertificates(getClass().getResourceAsStream(certFileLocation));
                } else {
                    return trustManagerForCertificates();
                }
            } catch (Exception e) {
                throw new ParamException("请求报错",e);
            }
        }
        @SuppressWarnings("all")
        public SSLSocketFactory getSSLSocketFactory() {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{getX509TrustManager()}, new SecureRandom());
                return sslContext.getSocketFactory();
            } catch (Exception e) {
                throw new ParamException("请求报错",e);
            }
        }
        @SuppressWarnings("all")
        private X509TrustManager trustManagerForCertificates() {
            return new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        }

        private X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }

            // Put the certificates a key store.
            char[] password = "password".toCharArray();
            // work.
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }

            // Use it to build an X509 trust manager.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        }

        private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
            try {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                // By convention, 'null' creates an empty
                InputStream in = null;
                // key store.
                keyStore.load(in, password);
                return keyStore;
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}

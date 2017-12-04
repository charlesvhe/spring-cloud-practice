package com.github.charlesvhe.springcloud.practice.consumer;

import okhttp3.OkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by charles on 2017/5/22.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ConsumerApplication {

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
    }
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null,  new TrustManager[] { new TrustAllCerts() }, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    public static void main(String[] args) {
//        运行添加启动项 -Xbootclasspath/p:/Users/charles/.m2/repository/org/mortbay/jetty/alpn/alpn-boot/8.1.12-SNAPSHOT/alpn-boot-8.1.12-SNAPSHOT.jar
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(createSSLSocketFactory())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .build();

//        RestTemplate http11Template = new RestTemplate();
        RestTemplate http2Template = new RestTemplate(new OkHttp3ClientHttpRequestFactory(client));

//        String http11Response = http11Template.getForObject("https://localhost:8443/user", String.class);
        String http2Response = http2Template.getForObject("https://localhost:8443/user", String.class);

        System.out.println( "HTTP/1.1 : " + http2Response.contains("You are using HTTP/2 right now!") + "<br/>" +
                "HTTP/2 : " + http2Response.contains("You are using HTTP/2 right now!"));
//        SpringApplication.run(ConsumerApplication.class, args);
    }

}

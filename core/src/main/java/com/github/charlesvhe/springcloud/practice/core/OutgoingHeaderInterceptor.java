package com.github.charlesvhe.springcloud.practice.core;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by charles on 2017/6/9.
 */
public class OutgoingHeaderInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders preHeaders = CoreHystrixRequestContext.outgoingHeader.get();
        try {
            CoreHystrixRequestContext.outgoingHeader.set(request.getHeaders());
            return execution.execute(request, body);
        } finally {
            CoreHystrixRequestContext.outgoingHeader.set(preHeaders);
        }
    }
}

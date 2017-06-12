package com.github.charlesvhe.springcloud.practice.core;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by charles on 2017/6/9.
 */
public class OutgoingHeaderInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ConcurrentHashMap<String, Collection<String>> old = CoreHystrixRequestContext.outgoingHeader.get();
        try {
            CoreHystrixRequestContext.outgoingHeader.set(new ConcurrentHashMap<>(request.getHeaders()));
            return execution.execute(request, body);
        } finally {
            CoreHystrixRequestContext.outgoingHeader.set(old);
        }

    }
}

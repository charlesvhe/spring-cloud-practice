package com.github.charlesvhe.springcloud.practice.core;

import feign.Request;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by charles on 2017/6/16.
 */
@Aspect
@Component
public class OutgoingFeignHeaderAspect {
    @Around("execution(* org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient.execute(..))")
    public Object Interceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpHeaders preHeaders = CoreHystrixRequestContext.outgoingHeader.get();
        try {
            Request request = (Request) joinPoint.getArgs()[0];
            HttpHeaders httpHeaders = new HttpHeaders();
            for (Map.Entry<String, Collection<String>> entry : request.headers().entrySet()) {
                httpHeaders.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            CoreHystrixRequestContext.outgoingHeader.set(httpHeaders);
            return joinPoint.proceed();
        } finally {
            CoreHystrixRequestContext.outgoingHeader.set(preHeaders);
        }
    }
}

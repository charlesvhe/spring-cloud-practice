package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by charles on 2017/5/26.
 */
public class CoreHeaderInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CoreHeaderInterceptor.class);

    public static final String HEADER_LABEL = "x-label";
    public static final String HEADER_LABEL_SPLIT = ",";

    public static final HystrixRequestVariableDefault<List<String>> label = new HystrixRequestVariableDefault<>();


    public static void initHystrixRequestContext(String labels) {
        logger.info("label: " + labels);
        if (!HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.initializeContext();
        }

        if (!StringUtils.isEmpty(labels)) {
            CoreHeaderInterceptor.label.set(Arrays.asList(labels.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT)));
        } else {
            CoreHeaderInterceptor.label.set(Collections.emptyList());
        }
    }

    public static void shutdownHystrixRequestContext() {
        if (HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.getContextForCurrentThread().shutdown();
        }
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CoreHeaderInterceptor.initHystrixRequestContext(request.getHeader(CoreHeaderInterceptor.HEADER_LABEL));
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        CoreHeaderInterceptor.shutdownHystrixRequestContext();
    }
}

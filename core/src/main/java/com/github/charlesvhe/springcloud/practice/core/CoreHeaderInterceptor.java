package com.github.charlesvhe.springcloud.practice.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
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

    public static final ThreadLocal<List<String>> label = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String labels = request.getHeader(CoreHeaderInterceptor.HEADER_LABEL);
        logger.info("label: " + labels);
        CoreHeaderInterceptor.setLabel(labels);
        return true;
    }

    public static void setLabel(String labels) {
        if (!StringUtils.isEmpty(labels)) {
            CoreHeaderInterceptor.label.set(Arrays.asList(labels.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT)));
        } else {
            CoreHeaderInterceptor.label.set(Collections.emptyList());
        }
    }
}

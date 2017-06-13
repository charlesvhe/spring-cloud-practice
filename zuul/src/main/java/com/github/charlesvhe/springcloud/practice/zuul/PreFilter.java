package com.github.charlesvhe.springcloud.practice.zuul;

import com.github.charlesvhe.springcloud.practice.core.CoreHystrixRequestContext;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Charles on 2016/8/26.
 */
public class PreFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(PreFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        // 添加outgoing herder给zuul内部转发路由
        ConcurrentHashMap<String, Collection<String>> outgoingHeader = new ConcurrentHashMap<>();
        CoreHystrixRequestContext.outgoingHeader.set(outgoingHeader);

        Enumeration<String> headerNames = ctx.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            Enumeration<String> headers = ctx.getRequest().getHeaders(headerName);
            List<String> headerList = new ArrayList<>();
            while (headers.hasMoreElements()) {
                headerList.add(headers.nextElement());
            }

            outgoingHeader.put(headerName, headerList);
        }

        return null;
    }
}

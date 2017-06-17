package com.github.charlesvhe.springcloud.practice.zuul;

import com.github.charlesvhe.springcloud.practice.core.CoreHystrixRequestContext;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;

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
        ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(ctx.getRequest());
        CoreHystrixRequestContext.outgoingHeader.set(servletServerHttpRequest.getHeaders());

        return null;
    }
}

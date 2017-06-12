package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来传递header信息
 *      1. incomingHeader 外部请求过来时的header
 *      1. outgoingHeader 内部使用RestTemplate/FeignClient向外发送请求时的header
 * @see HystrixRequestContextServletFilter
 * Created by charles on 2017/6/9.
 */
public class CoreHystrixRequestContext {
    public static final HystrixRequestVariableDefault<ConcurrentHashMap<String, String>> incomingHeader = new HystrixRequestVariableDefault<>();
    public static final HystrixRequestVariableDefault<ConcurrentHashMap<String, Collection<String>>> outgoingHeader = new HystrixRequestVariableDefault<>();
}

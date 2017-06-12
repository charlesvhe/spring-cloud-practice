package com.github.charlesvhe.springcloud.practice.core;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.DefaultPropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Created by charles on 2017/5/25.
 */
@Configuration
public class CoreAutoConfiguration {
    @Bean
    public DefaultPropertiesFactory defaultPropertiesFactory(){
        return new DefaultPropertiesFactory();
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new OutgoingHeaderInterceptor());
        return restTemplate;
    }

    @Bean
    public FilterRegistrationBean hystrixRequestContextFilterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new HystrixRequestContextServletFilter());
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
        return filterRegistrationBean;
    }
}

package com.github.charlesvhe.springcloud.practice.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.DefaultPropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * feign作为可选组件，不强制配置
 * Created by charles on 2017/5/25.
 */
@Configuration
@ConditionalOnClass(value = {feign.Client.class})
public class CoreFeignAutoConfiguration {

    @Bean
    public OutgoingFeignHeaderAspect outgoingFeignHeaderAspect(){
        return new OutgoingFeignHeaderAspect();
    }
}

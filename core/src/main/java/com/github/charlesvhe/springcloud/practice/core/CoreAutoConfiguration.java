package com.github.charlesvhe.springcloud.practice.core;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by charles on 2017/5/25.
 */
@Configuration
@EnableWebMvc
@RibbonClients(defaultConfiguration = DefaultRibbonConfiguration.class)
public class CoreAutoConfiguration extends WebMvcConfigurerAdapter {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new CoreHttpRequestInterceptor());
        return restTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CoreHeaderInterceptor());
    }
}

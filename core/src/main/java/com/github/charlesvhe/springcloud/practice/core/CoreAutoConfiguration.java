package com.github.charlesvhe.springcloud.practice.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.cloud.netflix.ribbon.DefaultPropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by charles on 2017/5/25.
 */
@Configuration
@EnableWebMvc
public class CoreAutoConfiguration implements WebMvcConfigurer {

//    @Autowired
//    public OkHttpClientFactory okHttpClientFactory;

    @Bean
    public DefaultPropertiesFactory defaultPropertiesFactory() {
        return new DefaultPropertiesFactory();
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
//        RestTemplate restTemplate = new RestTemplate(
//                new OkHttp3ClientHttpRequestFactory(
//                        okHttpClientFactory.createBuilder(true).build()));
        restTemplate.getInterceptors().add(new CoreHttpRequestInterceptor());

        return restTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CoreHeaderInterceptor());
    }
}

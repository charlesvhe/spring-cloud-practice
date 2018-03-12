package com.github.charlesvhe.springcloud.practice.core;

import com.github.charlesvhe.springcloud.practice.core.feign.CharlesRequestInterceptor;
import com.github.charlesvhe.springcloud.practice.core.feign.CharlesSpringMvcContract;
import feign.Contract;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Created by charles on 2017/5/25.
 */
@Configuration
public class CoreAutoConfiguration {
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Contract charlesSpringMvcContract(ConversionService conversionService) {
        return new CharlesSpringMvcContract(Collections.emptyList(), conversionService);
    }

    @Bean
    public CharlesRequestInterceptor charlesRequestInterceptor() {
        return new CharlesRequestInterceptor();
    }
}

package com.github.charlesvhe.springcloud.practice.core.feign;

import feign.Contract;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import java.util.Collections;

/**
 * Created by charles on 2017/5/25.
 */
@ConditionalOnClass(FeignClient.class)
@Configuration
public class FeignAutoConfiguration {
    @Bean
    public Contract charlesSpringMvcContract(ConversionService conversionService) {
        return new CharlesSpringMvcContract(Collections.emptyList(), conversionService);
    }

    @Bean
    public CharlesRequestInterceptor charlesRequestInterceptor() {
        return new CharlesRequestInterceptor();
    }
}

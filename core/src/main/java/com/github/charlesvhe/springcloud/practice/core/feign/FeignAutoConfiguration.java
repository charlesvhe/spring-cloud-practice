package com.github.charlesvhe.springcloud.practice.core.feign;

import feign.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charles on 2017/5/25.
 */
@ConditionalOnClass(FeignClient.class)
@Configuration
public class FeignAutoConfiguration {
    @Bean
    public Contract charlesSpringMvcContract(@Autowired(required = false) List<AnnotatedParameterProcessor> parameterProcessors,
                                             ConversionService conversionService) {
        if (null == parameterProcessors) {
            parameterProcessors = new ArrayList<>();
        }

        return new CharlesSpringMvcContract(parameterProcessors, conversionService);
    }

    @Bean
    public CharlesRequestInterceptor charlesRequestInterceptor() {
        return new CharlesRequestInterceptor();
    }
}

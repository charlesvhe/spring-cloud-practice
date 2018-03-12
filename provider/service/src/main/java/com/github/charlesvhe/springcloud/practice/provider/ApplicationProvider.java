package com.github.charlesvhe.springcloud.practice.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// 为了在dubbo impl服务中直接使用@Transactional注解 proxyTargetClass必须为true
@SpringBootApplication
@EnableDiscoveryClient
//@EnableTransactionManagement(proxyTargetClass = true)
@EnableSwagger2
public class ApplicationProvider {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationProvider.class, args);
    }

    @Bean
    public Docket docket(@Value("${swagger.enable:true}") boolean enableSwagger) {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(
                        new ApiInfoBuilder()
                                .title("服务提供者2.0")
                                .description("当前API版本" + ProviderApiAutoConfig.CURRENT_VERSION + " 兼容API版本" + ProviderApiAutoConfig.COMPATIBLE_VERSION)
                                .version(ProviderApiAutoConfig.CURRENT_VERSION)
                                .build())
                .select().apis(RequestHandlerSelectors.basePackage(ApplicationProvider.class.getPackage().getName()))
                .build().enable(enableSwagger);
    }
}

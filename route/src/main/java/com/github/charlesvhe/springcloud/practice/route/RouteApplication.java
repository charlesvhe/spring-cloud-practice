package com.github.charlesvhe.springcloud.practice.route;

import com.github.charlesvhe.springcloud.practice.core.CoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by charles on 2017/5/22.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@Import(CoreAutoConfiguration.class)
public class RouteApplication {
    public static void main(String[] args) {
        SpringApplication.run(RouteApplication.class, args);
    }

}

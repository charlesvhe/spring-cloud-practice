package com.github.charlesvhe.springcloud.practice.eureka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by charles on 2017/5/22.
 */
@SpringBootApplication
@EnableEurekaServer
@RestController
public class EurekaApplication {
    private static final Logger logger = LoggerFactory.getLogger(EurekaApplication.class);

    @RequestMapping("/test")
    public String test(){
        logger.info("/test");
        return "test: "+System.currentTimeMillis();
    }
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }

}

package com.github.charlesvhe.springcloud.practice.consumer.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by charles on 2017/5/25.
 */
@RestController
@RequestMapping("/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private AtomicInteger count = new AtomicInteger();
    private int preCount = 0;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.GET)
    public String test() {
        String result = restTemplate.getForObject("http://provider/user", String.class);
        return result;
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public String count(int thread, String api) {
        for (int i = 0; i < thread; i++) {
            new Thread() {
                @Override
                public void run() {
                    while (true) {
//                        new RestTemplate().getForObject("http://127.0.0.1:8080/user/" + api, String.class);
                        restTemplate.getForObject("http://provider/user/" + api, String.class);
                        count.incrementAndGet();
                    }
                }
            }.start();
        }

        return System.currentTimeMillis() + "";
    }

    @Scheduled(fixedRate=1000)
    public void log(){
        int curCount = count.get();
        logger.info("count: " + (curCount-preCount));
        preCount = curCount;
    }
}

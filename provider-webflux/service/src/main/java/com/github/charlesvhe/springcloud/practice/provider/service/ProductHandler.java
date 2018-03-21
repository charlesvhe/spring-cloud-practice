package com.github.charlesvhe.springcloud.practice.provider.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class ProductHandler {

    @GetMapping("/test")
    public Mono<String> hello() {
        Mono<String> just = Mono.create(sink -> {
            // 模拟IO
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Hello World");
            sink.success("Hello World");
        });

        just.publishOn(Schedulers.elastic());
        System.out.println("test done!");
        return just;
    }
}

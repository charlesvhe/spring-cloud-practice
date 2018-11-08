package com.github.charlesvhe.springcloud.practice.provider;

import com.netflix.discovery.DiscoveryClient;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;


/**
 * Created by charles on 2017/5/22.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProviderApplication {
    @Bean
    public DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs() {
        DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs = new DiscoveryClient.DiscoveryClientOptionalArgs();
        discoveryClientOptionalArgs.setAdditionalFilters(Arrays.asList(new ClientFilter(){
            @Override
            public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
                clientRequest.getHeaders().add("group","charles");
                return this.getNext().handle(clientRequest);
            }
        }));
        return discoveryClientOptionalArgs;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}

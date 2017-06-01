package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Created by charles on 2017/5/22.
 */
public class DefaultRibbonConfiguration {
    @Value("${ribbon.client.name:#{null}}")
    private String name;

    @Autowired(required = false)
    private IClientConfig config;

    @Autowired
    private PropertiesFactory propertiesFactory;

    @Bean
    public IRule ribbonRule() {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        if (this.propertiesFactory.isSet(IRule.class, name)) {
            return this.propertiesFactory.get(IRule.class, config, name);
        }

        // 默认配置
        LabelAndWeightMetadataRule rule = new LabelAndWeightMetadataRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }
}

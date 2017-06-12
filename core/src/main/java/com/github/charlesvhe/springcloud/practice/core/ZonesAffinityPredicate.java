package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PredicateKey;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by charles on 2017/6/9.
 */
public class ZonesAffinityPredicate extends AbstractServerPredicate {
    public static final String CORE_ZONES = "coreZones";

    private List<String> zones;

    public ZonesAffinityPredicate(IRule rule, IClientConfig clientConfig) {
        super(rule, clientConfig);

        // 优先使用个性化配置 <client>.ribbon.*
        String clientZones = (String) clientConfig.getProperties().get(CORE_ZONES);
        if (StringUtils.hasText(clientZones)) {   // 优先使用个性化配置
            this.zones = Arrays.asList(StringUtils.tokenizeToStringArray(clientZones, ","));
            return;
        }

        // 读取全局配置 ribbon.*
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        Configuration ribbon = config.subset(clientConfig.getNameSpace());
        String clientsZones = ribbon.getString(ZonesAffinityPredicate.CORE_ZONES);

        if (StringUtils.hasText(clientsZones)) {   // 使用全局配置
            this.zones = Arrays.asList(StringUtils.tokenizeToStringArray(clientsZones, ","));
        }

        // 仅使用本zone
        String zone = ConfigurationManager.getDeploymentContext().getValue(DeploymentContext.ContextKey.zone);
        this.zones = Arrays.asList(zone);
    }


    @Override
    public boolean apply(PredicateKey input) {
        if (CollectionUtils.isEmpty(this.zones)){
            return true;
        }

        return zones.contains(input.getServer().getZone());
    }
}

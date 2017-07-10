package com.github.charlesvhe.springcloud.practice.core;

import com.google.common.cache.Cache;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by charles on 2017/6/9.
 */
public class ZoneMappingRule extends PredicateBasedRule {
    public static final String CORE_ZONE_MAPPING_CACHE_SECONDS = "coreZoneMappingCacheSeconds";

    private CompositePredicate compositePredicate;
    @Autowired
    private Cache<String, String> zoneCache;
    private Random random = new Random();

    public ZoneMappingRule() {
        super();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        this.compositePredicate = CompositePredicate
                .withPredicates(new AvailabilityPredicate(this, clientConfig))
                // FIXME if no header fallback what?
//                .addFallbackPredicate(new ZonesAffinityPredicate(this, clientConfig))
                .build();
    }

    @Override
    public AbstractServerPredicate getPredicate() {
        return this.compositePredicate;
    }

    @Override
    public Server choose(Object key) {
        List<Server> serverList = this.getPredicate().getEligibleServers(this.getLoadBalancer().getAllServers(), key);
        if (CollectionUtils.isEmpty(serverList)) {
            return null;
        }

        String zone = getZone();

        List<Server> zoneServerList = new ArrayList<>();
        for (Server server : serverList) {
            if (zone.equals(server.getZone())) {
                zoneServerList.add(server);
            }
        }
        if (CollectionUtils.isEmpty(zoneServerList)) {
            throw new RuntimeException("no server for zone: " + zone);
        }
        return zoneServerList.get(random.nextInt(zoneServerList.size()));
    }

    private String getZone() {
        // get zone from cache
        HttpHeaders header = CoreHystrixRequestContext.outgoingHeader.get();
        String service = ((BaseLoadBalancer) this.getLoadBalancer()).getClientConfig().getClientName();

        // sub 非必填
        String sub = header.getFirst(CoreHystrixRequestContext.OUT_HEADER_ROUTE_SUB);

        // strategy 必填
        String strategy = header.getFirst(CoreHystrixRequestContext.OUT_HEADER_ROUTE_STRATEGY);

        // content 必填
        String content = header.getFirst(CoreHystrixRequestContext.OUT_HEADER_ROUTE_CONTENT);

        String cacheKey = ZoneMappingRule.getCacheKey(service, sub, strategy, content);
        String zone = this.zoneCache.getIfPresent(cacheKey);

        if (StringUtils.isEmpty(zone)) {
            throw new RuntimeException("no zone for key: " + cacheKey);
        }
        return zone;
    }

    public static String getCacheKey(String service, String sub, String strategy, String content) {
        return service + "_" + sub + "_" + strategy + "_" + content;
    }
}

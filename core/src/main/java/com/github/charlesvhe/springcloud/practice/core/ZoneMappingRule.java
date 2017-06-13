package com.github.charlesvhe.springcloud.practice.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.springframework.cloud.netflix.ribbon.DefaultPropertiesFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by charles on 2017/6/9.
 */
public class ZoneMappingRule extends PredicateBasedRule {
    public static final String CORE_ZONE_MAPPING_CACHE_SECONDS = "coreZoneMappingCacheSeconds";

    private CompositePredicate compositePredicate;
    private Cache<String, String> zoneCache;
    private Random random = new Random();

    public ZoneMappingRule() {
        super();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        String strSeconds = DefaultPropertiesFactory.getClientConfig(clientConfig, ZoneMappingRule.CORE_ZONE_MAPPING_CACHE_SECONDS);
        int seconds = 1200;
        try {
            seconds = Integer.parseInt(strSeconds);
        } catch (Exception ex) {
            // do nothing
        }
        this.zoneCache = CacheBuilder.newBuilder().expireAfterAccess(seconds, TimeUnit.SECONDS).build();

        // FIXME 添加测试缓存
        this.zoneCache.put(getCacheKey("provider",null, "mapping", "1001"),"r1z1");
        this.zoneCache.put(getCacheKey("provider",null, "mapping", "1002"),"r1z2");
        this.zoneCache.put(getCacheKey("provider",null, "mapping", "1003"),"r1z1");

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
        ConcurrentHashMap<String, Collection<String>> header = CoreHystrixRequestContext.outgoingHeader.get();
        String service = ((BaseLoadBalancer) this.getLoadBalancer()).getClientConfig().getClientName();

        // sub 非必填
        Collection<String> subList = header.get(CoreHystrixRequestContext.OUT_HEADER_ROUTE_SUB);
        String sub = null;
        if (!CollectionUtils.isEmpty(subList)) {
            sub = subList.iterator().next();
        }

        // strategy 必填
        Collection<String> strategyList = header.get(CoreHystrixRequestContext.OUT_HEADER_ROUTE_STRATEGY);
        if (CollectionUtils.isEmpty(strategyList)) {
            return null;
        }
        String strategy = strategyList.iterator().next();

        // content 必填
        Collection<String> contentList = header.get(CoreHystrixRequestContext.OUT_HEADER_ROUTE_CONTENT);
        if (CollectionUtils.isEmpty(contentList)) {
            return null;
        }
        String content = contentList.iterator().next();

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

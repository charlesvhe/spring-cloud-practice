package com.github.charlesvhe.springcloud.practice.route.service;

import com.github.charlesvhe.springcloud.practice.route.dao.RuleDao;
import com.github.charlesvhe.springcloud.practice.route.entity.Rule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.concurrent.TimeUnit;

/**
 * Created by charles on 2017/6/8.
 */
@Service
@Transactional
public class RuleService {
    private Cache<String, String> zoneCache;

    @Autowired
    private RuleDao ruleDao;

    public RuleService() {
        this.zoneCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    }

    public Rule save(Rule rule) {
        Rule savedRule = ruleDao.save(rule);
        if (null != savedRule) {
            updateZoneCache(savedRule);
        }
        return savedRule;
    }

    public Rule delete(Long id) {
        Rule rule = ruleDao.findOne(id);
        if (null != rule) {
            ruleDao.delete(rule);
            zoneCache.invalidate(RuleService.getKey(rule.getService(), rule.getStrategy(), rule.getContent()));
        }
        return rule;
    }

    public Rule update(Long id, Rule rule) {
        this.delete(rule.getId());
        rule.setId(null);
        return this.save(rule);
    }

    public Rule findOne(Long id) {
        Rule rule = ruleDao.findOne(id);
        if (null != rule) {
            updateZoneCache(rule);
        }
        return rule;
    }

    public String findZone(String service, String strategy, String content) {
        String zone = zoneCache.getIfPresent(RuleService.getKey(service, strategy, content));
        if (StringUtils.isEmpty(zone)) {
            Rule example = new Rule();
            example.setService(service);
            example.setStrategy(strategy);
            example.setContent(content);

            Rule rule = ruleDao.findOne(Example.of(example));

            if(rule != null){
                updateZoneCache(rule);
                zone = rule.getZone();
            }
        }
        return zone;
    }


    private void updateZoneCache(Rule rule) {
        zoneCache.put(RuleService.getKey(rule.getService(), rule.getStrategy(), rule.getContent()), rule.getZone());
    }

    public static String getKey(String service, String strategy, String content) {
        return "prefix_" + service + "_" + strategy + "_" + content;
    }

}

package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LabelOrPredicate extends AbstractServerPredicate {
    public static final String META_DATA_KEY_LABEL_OR = "labelOr";

    @Override
    public boolean apply(PredicateKey predicateKey) {
        List<String> headerLabels = CoreHeaderInterceptor.label.get();
        // 请求没有label不做实例选择 直接匹配
        if (CollectionUtils.isEmpty(headerLabels)) {
            return true;
        }

        Map<String, String> metadata = ((DiscoveryEnabledServer) predicateKey.getServer()).getInstanceInfo().getMetadata();
        String labelAnd = metadata.get(META_DATA_KEY_LABEL_OR);
        // 实例没有label meta视为匹配
        if (StringUtils.isEmpty(labelAnd)) {
            return true;
        }
        List<String> metadataLabels = Arrays.asList(labelAnd.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT));
        for (String metadataLabel : metadataLabels) {
            if(headerLabels.contains(metadataLabel)){
                return true;
            }
        }
        return false;
    }
}

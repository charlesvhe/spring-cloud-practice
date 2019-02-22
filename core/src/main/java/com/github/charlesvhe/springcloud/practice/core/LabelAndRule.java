package com.github.charlesvhe.springcloud.practice.core;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;

public class LabelAndRule extends PredicateBasedRule {
    private CompositePredicate compositePredicate;

    public LabelAndRule() {
        super();
        compositePredicate = createCompositePredicate(
                new ZoneAvoidancePredicate(this, null),
                new AvailabilityPredicate(this, null),
                new LabelAndPredicate());
    }

    private CompositePredicate createCompositePredicate(ZoneAvoidancePredicate p1, AvailabilityPredicate p2, LabelAndPredicate p3) {
        return CompositePredicate.withPredicates(p1, p2, p3)
                .addFallbackPredicate(CompositePredicate.withPredicates(p1, p2).build())
                .addFallbackPredicate(p2)
                .addFallbackPredicate(AbstractServerPredicate.alwaysTrue())
                .build();
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        compositePredicate = createCompositePredicate(
                new ZoneAvoidancePredicate(this, clientConfig),
                new AvailabilityPredicate(this, clientConfig),
                new LabelAndPredicate());
    }

    @Override
    public AbstractServerPredicate getPredicate() {
        return compositePredicate;
    }
}

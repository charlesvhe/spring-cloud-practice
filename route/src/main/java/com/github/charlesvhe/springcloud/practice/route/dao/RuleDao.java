package com.github.charlesvhe.springcloud.practice.route.dao;

import com.github.charlesvhe.springcloud.practice.route.entity.Rule;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

/**
 * Created by charles on 2017/6/8.
 */
@Repository
public class RuleDao extends SimpleJpaRepository<Rule, Long> {
    private EntityManager entityManager;

    public RuleDao(EntityManager entityManager) {
        super(Rule.class, entityManager);
        this.entityManager = entityManager;
    }
}

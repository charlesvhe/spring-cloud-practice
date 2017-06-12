package com.github.charlesvhe.springcloud.practice.route.controller;

import com.github.charlesvhe.springcloud.practice.route.entity.Rule;
import com.github.charlesvhe.springcloud.practice.route.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Created by charles on 2017/6/8.
 */
@RestController
@RequestMapping("/rule")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.POST)
    public Rule save(@RequestBody Rule rule) {
        return ruleService.save(rule);
    }

    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.DELETE)
    public Rule delete(@PathVariable Long id) {
        return ruleService.delete(id);
    }

    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.PUT)
    public Rule update(@PathVariable Long id, @RequestBody Rule rule) {
        rule.setId(id);
        return ruleService.update(id, rule);
    }

    @RequestMapping(value = "/{id:\\d+}", method = RequestMethod.GET)
    public Rule findOne(@PathVariable Long id) {
        String result = restTemplate.getForObject("http://provider/user", String.class);
        System.out.println(result);
        return ruleService.findOne(id);
    }

    @RequestMapping(value = "/zone", method = RequestMethod.GET)
    public String findZone(String service, String strategy, String content) {
        return ruleService.findZone(service, strategy, content);
    }

}

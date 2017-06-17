package com.github.charlesvhe.springcloud.practice.consumer.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by charles on 2017/5/25.
 */
@FeignClient("provider")
@RequestMapping("/user")
public interface UserService {

    @RequestMapping(method = RequestMethod.GET)
    public List<User> query();

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User query(@PathVariable("id") Long id);

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public List<User> query(@RequestBody User user);

    @RequestMapping(method = RequestMethod.POST)
    public User insert(@RequestBody User user);

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public User delete(@PathVariable("id") Long id);

    @RequestMapping(method = RequestMethod.PUT)
    public User update(@RequestBody User user);
}
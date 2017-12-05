package com.github.charlesvhe.springcloud.practice.provider.controller;

import com.github.charlesvhe.springcloud.practice.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by charles on 2017/5/25.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private List<User> list10 = new ArrayList<>(10);
    private List<User> list100 = new ArrayList<>(50);
    private List<User> list1000 = new ArrayList<>(100);


    public UserController() {
        for (int i = 0; i < 10; i++) {
            list10.add(new User((long)i, "account"+i, "password"+i));
        }

        for (int i = 0; i < 100; i++) {
            list100.add(new User((long)i, "account"+i, "password"+i));
        }

        for (int i = 0; i < 1000; i++) {
            list1000.add(new User((long)i, "account"+i, "password"+i));
        }
    }

    @RequestMapping(value = "/test10", method = RequestMethod.GET)
    public List<User> test10() {
        return list10;
    }

    @RequestMapping(value = "/test100", method = RequestMethod.GET)
    public List<User> test100() {
        return list100;
    }

    @RequestMapping(value = "/test1000", method = RequestMethod.GET)
    public List<User> test1000() {
        return list1000;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> query(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder sbHeader = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            sbHeader.append(" [" + headerName + ":" + request.getHeader(headerName) + "] ");
        }

        logger.info("query all " + request.getProtocol() + " " + sbHeader);
        return Arrays.asList(new User(1L, "account1", "password1"),
                new User(2L, "account2", "password2"),
                new User(3L, "account3", "password3"));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User query(@PathVariable Long id) {
        logger.info("query by id");
        return new User(id, "account" + id, "password" + id);
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public List<User> query(@RequestBody User user) {
        logger.info("query by example");
        return Arrays.asList(user);
    }

    @RequestMapping(method = RequestMethod.POST)
    public User insert(@RequestBody User user) {
        logger.info("insert");
        return user;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public User delete(@PathVariable Long id) {
        logger.info("delete by id");
        return new User(id, "account" + id, "password" + id);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public User update(@RequestBody User user) {
        logger.info("update");
        return user;
    }
}
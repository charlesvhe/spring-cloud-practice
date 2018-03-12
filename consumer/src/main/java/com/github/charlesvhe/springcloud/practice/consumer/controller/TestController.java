package com.github.charlesvhe.springcloud.practice.consumer.controller;


import com.github.charlesvhe.springcloud.practice.core.vo.PageData;
import com.github.charlesvhe.springcloud.practice.core.vo.Response;
import com.github.charlesvhe.springcloud.practice.provider.entity.Product;
import com.github.charlesvhe.springcloud.practice.provider.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by charles on 2017/5/25.
 */
@RestController
@RequestMapping("/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private ProductService productService;

    @RequestMapping(method = RequestMethod.GET)
    public Response<PageData<Product, Product>> test() {
        ProductService.Page page = new ProductService.Page();
        page.setOffset(100);
        page.setLimit(5);

        Product filter = new Product();
        filter.setCategoryId(2L);
        page.setFilter(filter);


        return productService.selectAllGet(page);
    }
}

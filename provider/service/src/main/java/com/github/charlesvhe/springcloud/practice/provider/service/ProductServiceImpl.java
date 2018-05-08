package com.github.charlesvhe.springcloud.practice.provider.service;


import com.github.charlesvhe.springcloud.practice.core.vo.PageData;
import com.github.charlesvhe.springcloud.practice.core.vo.Response;
import com.github.charlesvhe.springcloud.practice.provider.entity.Product;
import com.github.charlesvhe.springcloud.practice.provider.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    // FIXME 进入开发前, 提供mock数据给前端测试API使用 开发完后删除mock
    private static final long TOTAL = 5L;
    private List<Product> mock = new ArrayList<>();
    {
        for (int i = 0; i < TOTAL; i++) {
            Product product = new Product(Integer.toUnsignedLong(i), 2L, "name" + i, "description" + i, "http://imageurl" + i);
            mock.add(product);
        }
    }

    @Override
    public List<Product> selectAll(Integer offset, Integer limit) {

        return productRepository.findAll();
//        return mock;
    }

    @Override
    public Response<PageData<Product, Product>> selectAll(Page page) {
        return new Response(new PageData<Product, Product>(mock, page.buildNextPage(TOTAL)));
    }

    @Override
    public Response<PageData<Product, Product>> selectAllGet(Page page) {
        return new Response(new PageData<Product, Product>(mock, page.buildNextPage(TOTAL)));
    }

    @Override
    public Response<Product> selectById(Long id) {
        return new Response(new Product(id, 2L, "name" + id, "description" + id, "http://imageurl" + id));
    }

    @Override
    public Response<Product> insert(Product product) {
        product.setId(1L);
        return new Response(product);
    }

    @Override
    public Response<Product> delete(Long id) {
        return new Response(new Product(id, 2L, "name" + id, "description" + id, "http://imageurl" + id));
    }

    @Override
    public Response<Product> update(Product product) {
        return new Response(product);
    }
}

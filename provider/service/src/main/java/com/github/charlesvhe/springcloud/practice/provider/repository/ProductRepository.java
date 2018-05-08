package com.github.charlesvhe.springcloud.practice.provider.repository;

import com.github.charlesvhe.springcloud.practice.provider.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

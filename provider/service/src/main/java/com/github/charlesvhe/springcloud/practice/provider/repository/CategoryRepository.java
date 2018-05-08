package com.github.charlesvhe.springcloud.practice.provider.repository;

import com.github.charlesvhe.springcloud.practice.provider.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
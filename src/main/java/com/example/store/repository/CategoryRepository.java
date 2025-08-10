package com.example.store.repository;

import com.example.store.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // پیدا کردن تمام دسته‌هایی که پدر ندارند (ریشه‌ها)
    List<Category> findByParentIsNull();
}


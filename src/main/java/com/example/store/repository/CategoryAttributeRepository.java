package com.example.store.repository;

import com.example.store.model.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    List<CategoryAttribute> findByCategoryId(Long categoryId);
}

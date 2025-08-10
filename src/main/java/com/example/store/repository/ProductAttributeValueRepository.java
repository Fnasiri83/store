package com.example.store.repository;

import com.example.store.model.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    List<ProductAttributeValue> findByProductId(Long productId);
}

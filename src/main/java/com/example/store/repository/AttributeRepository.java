package com.example.store.repository;

import com.example.store.model.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    boolean existsByName(String name);
}

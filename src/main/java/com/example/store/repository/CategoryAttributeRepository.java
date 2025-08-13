package com.example.store.repository;
import com.example.store.model.Category;
import com.example.store.model.CategoryAttribute;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
    List<CategoryAttribute> findByCategoryId(Long categoryId);

    @Query("DELETE FROM CategoryAttribute ca WHERE ca.category.id = :categoryId")
    @Modifying
    @Transactional
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
    // متد جدید برای حذف بر اساس attributeId
    @Query("DELETE FROM CategoryAttribute ca WHERE ca.attribute.id = :attributeId")
    @Modifying
    @Transactional
    void deleteByAttributeId(@Param("attributeId") Long attributeId);
    void deleteByCategory(@NotNull Category category);
    List<CategoryAttribute> findByAttributeId(Long attributeId);

}

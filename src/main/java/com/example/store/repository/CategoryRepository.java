package com.example.store.repository;

import com.example.store.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

        // پیدا کردن تمام دسته‌هایی که والد ندارند (ریشه‌ها)
        List<Category> findByParentIsNull();

        // پیدا کردن مسیر والدها برای یک دسته‌بندی خاص (CTE بازگشتی)
        @Query(value = "WITH RECURSIVE category_path AS (" +
                "    SELECT id, parent_id, 0 as depth " +
                "    FROM category " + // 👈 دقت کن اسم جدولت category هست نه categories
                "    WHERE id = :categoryId " +
                "    UNION ALL " +
                "    SELECT c.id, c.parent_id, cp.depth + 1 " +
                "    FROM category c " +
                "    INNER JOIN category_path cp ON c.id = cp.parent_id " + // 👈 درست شد
                ") " +
                "SELECT id " +
                "FROM category_path " +
                "WHERE id != :categoryId " +
                "ORDER BY depth ASC",
                nativeQuery = true)
        List<Long> findParentIds(@Param("categoryId") Long categoryId);
}
//        @Query(value = "WITH RECURSIVE category_path AS (" +
//                "    SELECT id, parent_id, 0 as depth " +
//                "    FROM categories " +
//                "    WHERE id = :categoryId " +
//                "    UNION ALL " +
//                "    SELECT c.id, c.parent_id, cp.depth + 1 " +
//                "    FROM categories c " +
//                "    INNER JOIN category_path cp ON cp.parent_id = c.id " +
//                ") " +
//                "SELECT id " +
//                "FROM category_path " +
//                "WHERE id != :categoryId " +
//                "ORDER BY depth ASC", nativeQuery = true)
//        @Query(value = "WITH RECURSIVE category_path AS (" +
//                "  SELECT id, parent_id " +
//                "  FROM categories " +
//                "  WHERE id = :categoryId " +
//                "  UNION ALL " +
//                "  SELECT c.id, c.parent_id " +
//                "  FROM categories c " +
//                "  INNER JOIN category_path cp ON c.id = cp.parent_id " +
//                "  WHERE c.parent_id IS NOT NULL " +
//                ") SELECT id FROM category_path WHERE id != :categoryId ORDER BY parent_id ASC",
//                nativeQuery = true)
//public interface CategoryRepository extends JpaRepository<Category, Long> {
//
//
//        // پیدا کردن تمام دسته‌هایی که والد ندارند (ریشه‌ها)
//        List<Category> findByParentIsNull();
//
//        // پیدا کردن مسیر والدها برای یک دسته‌بندی خاص (CTE بازگشتی)
//        @Query(value = "WITH RECURSIVE category_path AS (" +
//                "  SELECT id, parent_id " +
//                "  FROM categories " +
//                "  WHERE id = :categoryId " +
//                "  UNION ALL " +
//                "  SELECT c.id, c.parent_id " +
//                "  FROM categories c " +
//                "  INNER JOIN category_path cp ON c.id = cp.parent_id " +
//                "  WHERE c.parent_id IS NOT NULL " +
//                ") SELECT id FROM category_path WHERE id != :categoryId", nativeQuery = true)
//        List<Long> findParentIds(@Param("categoryId") Long categoryId);
//    }

//    // پیدا کردن تمام دسته‌هایی که پدر ندارند (ریشه‌ها)
//    List<Category> findByParentIsNull();
//
//    // پیدا کردن مسیر والدها برای یک دسته‌بندی خاص (CTE بازگشتی)
//    @Query(value = "WITH RECURSIVE category_path AS (" +
//            "  SELECT id, parent_id " +
//            "  FROM categories " +
//            "  WHERE id = :categoryId " +
//            "  UNION ALL " +
//            "  SELECT c.id, c.parent_id " +
//            "  FROM categories c " +
//            "  INNER JOIN category_path cp ON c.id = cp.parent_id " +
//            ") SELECT id FROM category_path WHERE id != :categoryId", nativeQuery = true) // بدون خود دسته
//    List<Long> findParentIds(@Param("categoryId") Long categoryId);

//
//public interface CategoryRepository extends JpaRepository<Category, Long> {
//    // پیدا کردن تمام دسته‌هایی که پدر ندارند (ریشه‌ها)
//    List<Category> findByParentIsNull();
//
//    // پیدا کردن مسیر والدها برای یک دسته‌بندی خاص
//    @Query(value = "WITH RECURSIVE category_path AS (" +
//            "  SELECT id, parent_id " +
//            "  FROM categories " +
//            "  WHERE id = :categoryId " +
//            "  UNION ALL " +
//            "  SELECT c.id, c.parent_id " +
//            "  FROM categories c " +
//            "  INNER JOIN category_path cp ON c.id = cp.parent_id " +
//            ") SELECT id FROM category_path", nativeQuery = true)
//    List<Long> findParentIds(@Param("categoryId") Long categoryId);

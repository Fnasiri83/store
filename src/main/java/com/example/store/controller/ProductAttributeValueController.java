package com.example.store.controller;

import com.example.store.dto.ProductAttributeValueCreateDTO;
import com.example.store.dto.ProductAttributeValueDTO;
import com.example.store.service.ProductAttributeValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/product-attribute-values")
@RequiredArgsConstructor
public class ProductAttributeValueController {

    private final ProductAttributeValueService productAttributeValueService;

    /**
     * 📌 ایجاد مقدار ویژگی جدید برای یک محصول
     * مثال آدرس: POST /api/product-attribute-values/{productId}
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ProductAttributeValueDTO> create(
            @PathVariable Long productId,
            @RequestBody ProductAttributeValueCreateDTO createDTO) {
        System.out.println("create=ProductAttributeValueController ");
        return ResponseEntity.ok(productAttributeValueService.create(productId, createDTO));
    }

    /**
     * 📌 دریافت همه مقادیر ویژگی‌های یک محصول
     * مثال آدرس: GET /api/product-attribute-values/by-product/{productId}
     */
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<ProductAttributeValueDTO>> getByProduct(
            @PathVariable Long productId) {
        System.out.println("getByProduct=ProductAttributeValueController ");
        return ResponseEntity.ok(productAttributeValueService.getByProduct(productId));
    }

    /**
     * 📌 بروزرسانی مقدار ویژگی
     * مثال آدرس: PUT /api/product-attribute-values/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductAttributeValueDTO> update(
            @PathVariable Long id,
            @RequestBody ProductAttributeValueCreateDTO updateDTO) {
        System.out.println("update=ProductAttributeValueController ");
        return ResponseEntity.ok(productAttributeValueService.update(id, updateDTO));
    }

    /**
     * 📌 حذف مقدار ویژگی
     * مثال آدرس: DELETE /api/product-attribute-values/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productAttributeValueService.delete(id);
        System.out.println("delete=ProductAttributeValueController ");
        return ResponseEntity.noContent().build(); // وضعیت 204 بدون محتوا
    }
}

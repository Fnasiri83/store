package com.example.store.controller;

import com.example.store.dto.AttributeCreateDTO;
import com.example.store.dto.AttributeDTO;
import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.service.AttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;
    /**
     * ایجاد ویژگی جدید
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttributeDTO create(@Valid @RequestBody AttributeCreateDTO dto) {
        return attributeService.create(dto);
    }
    /**
     * دریافت لیست همه ویژگی‌ها
     */
    @GetMapping
    public List<AttributeDTO> getAll() {
        return attributeService.getAll();
    }
    /**
     * دریافت ویژگی بر اساس ID
     */
    @GetMapping("/{id}")
    public AttributeDTO getById(@PathVariable Long id) {
        return attributeService.getById(id);
    }
    /**
     * بروزرسانی ویژگی
     */
    @PutMapping("/{id}")
    public AttributeDTO update(@PathVariable Long id, @Valid @RequestBody AttributeCreateDTO dto) {
        return attributeService.update(id, dto);
    }
    /**
     * حذف ویژگی
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attributeService.delete(id);
    }

    /**
     * =============================
     * مدیریت CategoryAttribute
     * =============================
     */

    // دریافت ویژگی‌های یک دسته‌بندی (با ارث‌بری از والدها)
    @GetMapping("/categories/{categoryId}")
    public List<CategoryAttributeDTO> getCategoryAttributes(@PathVariable Long categoryId) {
        return attributeService.getCategoryAttributes(categoryId);
    }

    // افزودن ویژگی به یک دسته‌بندی
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryAttributeDTO addCategoryAttribute(@Valid @RequestBody CategoryAttributeCreateDTO dto) {
        return attributeService.addCategoryAttribute(dto);
    }

    // حذف ویژگی از یک دسته‌بندی
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryAttribute(@PathVariable Long id) {
        attributeService.deleteCategoryAttribute(id);
    }
}

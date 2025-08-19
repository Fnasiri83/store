package com.example.store.controller;

import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.service.CategoryAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-attributes")
@RequiredArgsConstructor
public class CategoryAttributeController {

    private final CategoryAttributeService categoryAttributeService;

    /**
     * اتصال ویژگی به دسته‌بندی
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryAttributeDTO create(@Valid @RequestBody CategoryAttributeCreateDTO dto) {
        System.out.println("createCategoryAttribute=CategoryAttributeController ");
        return categoryAttributeService.create(dto);
    }

    /**
     * دریافت همه ویژگی‌های یک دسته‌بندی بر اساس ID دسته‌بندی
     */
    @GetMapping("/category/{categoryId}")
    public List<CategoryAttributeDTO> getByCategoryId(@PathVariable Long categoryId) {
        System.out.println("getByCategoryId=CategoryAttributeController (ارث بری) ");
        return categoryAttributeService.getByCategoryId(categoryId);
    }

    /**
     * حذف یک ویژگی از دسته‌بندی
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        System.out.println("deleteCategoryAttribute=CategoryAttributeController ");
        categoryAttributeService.delete(id);
    }
}

package com.example.store.controller;

import com.example.store.dto.CategoryDTO;
import com.example.store.dto.CategoryTreeNodeDTO;
import com.example.store.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * ایجاد دسته‌بندی جدید
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO create(@Valid @RequestBody CategoryDTO dto) {
        return categoryService.create(dto);
    }

    /**
     * دریافت همه دسته‌بندی‌ها
     */
    @GetMapping
    public List<CategoryDTO> getAll() {
        return categoryService.getAll();
    }

    /**
     * دریافت دسته‌بندی‌ها به صورت درختی
     */
    @GetMapping("/tree")
    public List<CategoryTreeNodeDTO> getTree() {
        return categoryService.getTree();
    }

    /**
     * دریافت دسته‌بندی بر اساس ID
     */
    @GetMapping("/{id}")
    public CategoryDTO getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    /**
     * بروزرسانی دسته‌بندی
     */
    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return categoryService.update(id, dto);
    }

    /**
     * حذف دسته‌بندی
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cannot delete category because it has subcategories.");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable Long id) {
//        categoryService.delete(id);
//    }
}

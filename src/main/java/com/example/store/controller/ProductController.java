package com.example.store.controller;

import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * ایجاد محصول جدید
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProductDTO> create(@Valid @RequestBody ProductCreateDTO dto) {
        log.info("Received request to create product: {}", dto);
        try {
            ProductDTO createdProduct = productService.create(dto);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            throw e; // یا می‌توانید یک ResponseEntity با خطای مناسب برگردانید
        }
    }
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public ProductDTO create(@Valid @RequestBody ProductCreateDTO dto) {
//        return productService.create(dto);
//    }

    /**
     * دریافت همه محصولات
     */
    @GetMapping
    public List<ProductDTO> getAll() {
        return productService.getAll();
    }

    /**
     * دریافت محصول بر اساس ID
     */
    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /**
     * بروزرسانی محصول
     */
    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        return productService.update(id, dto);
    }

    /**
     * حذف محصول
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}

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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    /**
     * آپلود فایل (عکس یا گیف) برای محصول
     */
    @PostMapping("/{productId}/upload")
    public ResponseEntity<ProductDTO> uploadFile(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        log.info("درخواست آپلود فایل برای محصول با شناسه: {}", productId);
        ProductDTO productDTO = productService.uploadFile(productId, file);
        return ResponseEntity.status(HttpStatus.OK).body(productDTO);
    }


    /**
     * حذف فایل از محصول
     */
    @DeleteMapping("/{productId}/files")
    public ResponseEntity<ProductDTO> deleteFile(
            @PathVariable Long productId,
            @RequestParam("fileUrl") String fileUrl) {
        log.info("درخواست حذف فایل با مسیر: {} از محصول با شناسه: {}", fileUrl, productId);
        ProductDTO productDTO = productService.deleteFile(productId, fileUrl);
        return ResponseEntity.ok(productDTO);
    }

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
        System.out.println("getAll=ProductController ");
        return productService.getAll();
    }

    /**
     * دریافت محصول بر اساس ID
     */
    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable Long id) {
        System.out.println("getById=ProductController ");
        return productService.getById(id);
    }

    /**
     * بروزرسانی محصول
     */
    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        System.out.println("update=ProductController ");
        return productService.update(id, dto);
    }

    /**
     * حذف محصول
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        System.out.println("delete=ProductController ");
        productService.delete(id);
    }
}

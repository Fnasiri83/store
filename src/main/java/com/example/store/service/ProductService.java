package com.example.store.service;

import com.example.store.dto.ProductAttributeValueCreateDTO;
import com.example.store.dto.ProductAttributeValueDTO;
import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.mapper.ProductAttributeValueMapper;
import com.example.store.mapper.ProductMapper;
import com.example.store.model.Category;
import com.example.store.model.Product;
import com.example.store.model.ProductAttributeValue;
import com.example.store.repository.CategoryRepository;
import com.example.store.repository.ProductAttributeValueRepository;
import com.example.store.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final ProductAttributeValueService productAttributeValueService;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    /**
     * ایجاد محصول جدید
     */
    public ProductDTO create(ProductCreateDTO dto) {
        log.info("Received ProductCreateDTO: {}", dto);

        // 1. تبدیل DTO به Product
        Product product = productMapper.toEntity(dto);

        // 2. اطمینان از تنظیم category
        Category category = categoryRepository.findById(Long.valueOf(dto.getCategoryId()))
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        product.setCategory(category);

        System.out.println("عشق کن");

        // 3. اطمینان از مقداردهی اولیه attributeValues
        if (product.getAttributeValues() == null) {
            product.setAttributeValues(new ArrayList<>());
        }
        System.out.println(" که  داره کار میکنه برنامت");
        // 4. پردازش و ذخیره ویژگی‌ها قبل از ذخیره اصلی
        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
            product.getAttributeValues().clear(); // پاک کردن لیست موجود

            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
                ProductAttributeValue pav = productAttributeValueMapper.toEntity(attrDTO);
                pav.setProduct(product); // تنظیم رابطه دوطرفه
                product.getAttributeValues().add(pav);
            }
        }

        // 5. ذخیره Product (به همراه attributeValues به دلیل cascade)
        Product savedProduct = productRepository.save(product);

        // 6. تبدیل به DTO و بازگشت
        return productMapper.toDTO(savedProduct);
    }

//    public ProductDTO create(ProductCreateDTO dto) {
//        log.info("Received ProductCreateDTO: {}", dto);
//
//        // 1. تبدیل DTO به Product
//        Product product = productMapper.toEntity(dto);
//
//        // 2. اطمینان از تنظیم category
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // 3. اطمینان از مقداردهی اولیه attributeValues
//        if (product.getAttributeValues() == null) {
//            product.setAttributeValues(new ArrayList<>());
//        }
//
//        // 4. ذخیره اولیه Product
//        Product savedProduct = productRepository.save(product);
//
//        // 5. پردازش و ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            // پاک کردن لیست موجود (به جای جایگزینی مرجع)
//            savedProduct.getAttributeValues().clear();
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                log.info("Processing attribute: {}", attrDTO);
//                ProductAttributeValueDTO pavDTO = productAttributeValueService.create(savedProduct.getId(), attrDTO);
//                ProductAttributeValue pav = productAttributeValueRepository.findById(pavDTO.getId())
//                        .orElseThrow(() -> new RuntimeException("Failed to retrieve ProductAttributeValue"));
//                savedProduct.getAttributeValues().add(pav); // اضافه کردن به لیست موجود
//            }
//            // 6. ذخیره مجدد Product برای به‌روزرسانی رابطه
//            savedProduct = productRepository.save(savedProduct);
//        }
//
//        // 7. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        log.info("Received ProductCreateDTO: {}", dto);
//
//        // 1. تبدیل DTO به Product
//        Product product = productMapper.toEntity(dto);
//
//        // 2. اطمینان از تنظیم category
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // 3. تنظیم لیست attributeValues خالی
//        product.setAttributeValues(new ArrayList<>());
//
//        // 4. ذخیره اولیه Product
//        Product savedProduct = productRepository.save(product);
//
//        // 5. پردازش و ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            List<ProductAttributeValue> attributeValues = new ArrayList<>();
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                log.info("Processing attribute: {}", attrDTO);
//                ProductAttributeValueDTO pavDTO = productAttributeValueService.create(savedProduct.getId(), attrDTO);
//                ProductAttributeValue pav = productAttributeValueRepository.findById(pavDTO.getId())
//                        .orElseThrow(() -> new RuntimeException("Failed to retrieve ProductAttributeValue"));
//                attributeValues.add(pav);
//            }
//            savedProduct.setAttributeValues(attributeValues);
//            // 6. ذخیره مجدد Product برای به‌روزرسانی رابطه
//            savedProduct = productRepository.save(savedProduct);
//        }
//
//        // 7. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        log.info("Received ProductCreateDTO: {}", dto);
//
//        // 1. تبدیل DTO به Product
//        Product product = productMapper.toEntity(dto);
//
//        // 2. اطمینان از تنظیم category
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // 3. ذخیره اولیه Product
//        Product savedProduct = productRepository.save(product);
//
//        // 4. پردازش و ذخیره ویژگی‌ها و اضافه کردن به لیست attributeValues
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            List<ProductAttributeValue> attributeValues = new ArrayList<>();
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                ProductAttributeValueDTO pavDTO = productAttributeValueService.create(savedProduct.getId(), attrDTO);
//                ProductAttributeValue pav = productAttributeValueRepository.findById(pavDTO.getId())
//                        .orElseThrow(() -> new RuntimeException("Failed to retrieve ProductAttributeValue"));
//                attributeValues.add(pav);
//            }
//            savedProduct.setAttributeValues(attributeValues);
//            // 5. ذخیره مجدد Product برای به‌روزرسانی رابطه
//            savedProduct = productRepository.save(savedProduct);
//        }
//
//        // 6. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        // 1. تبدیل DTO به Product و ذخیره آن
//        Product product = productMapper.toEntity(dto);
//        Product savedProduct = productRepository.save(product);
//
//        // 2. پردازش و ذخیره ویژگی‌ها
//        if (dto.getAttributeValues() != null && !dto.getAttributeValues().isEmpty()) {
//            for (ProductAttributeValueCreateDTO attrDTO : dto.getAttributeValues()) {
//                productAttributeValueService.create(savedProduct.getId(), attrDTO);
//            }
//        }
//
//        // 3. تبدیل به DTO و بازگشت
//        return productMapper.toDTO(savedProduct);
//    }
//    @Transactional
//    public ProductDTO create(ProductCreateDTO dto) {
//        Category category = categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//        Product product = productMapper.toEntity(dto);
//        product.setCategory(category); // تنظیم category
//        return productMapper.toDTO(productRepository.save(product));
//    }
//    public ProductDTO create(ProductCreateDTO dto) {
//        Product product = productMapper.toEntity(dto);
//        return productMapper.toDTO(productRepository.save(product));
//    }

    /**
     * دریافت همه محصولات
     */
    public List<ProductDTO> getAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * دریافت محصول بر اساس ID
     */
    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return productMapper.toDTO(product);
    }

    /**
     * بروزرسانی محصول
     */
    public ProductDTO update(Long id, ProductCreateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        return productMapper.toDTO(productRepository.save(product));
    }

    /**
     * حذف محصول
     */
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found");
        }
        productAttributeValueRepository.deleteByProductId(id); // حذف وابستگی‌ها
        productRepository.deleteById(id);
    }

//    public void delete(Long id) {
//        if (!productRepository.existsById(id)) {
//            throw new EntityNotFoundException("Product not found");
//        }
//        productRepository.deleteById(id);
//    }
}

package com.example.store.service;

import com.example.store.dto.ProductAttributeValueCreateDTO;
import com.example.store.dto.ProductAttributeValueDTO;
import com.example.store.mapper.ProductAttributeValueMapper;
import com.example.store.model.Attribute;
import com.example.store.model.Product;
import com.example.store.model.ProductAttributeValue;
import com.example.store.repository.AttributeRepository;
import com.example.store.repository.ProductAttributeValueRepository;
import com.example.store.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAttributeValueService {

    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final ProductRepository productRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueMapper productAttributeValueMapper;

    /**
     * ایجاد مقدار ویژگی جدید برای یک محصول
     */

    @Transactional
    public ProductAttributeValueDTO create(Long productId, ProductAttributeValueCreateDTO createDTO) {
        log.info("Received productId: {}, ProductAttributeValueCreateDTO: {}", productId, createDTO);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("محصول پیدا نشد: " + productId));
        Attribute attribute = attributeRepository.findById(createDTO.getAttributeId())
                .orElseThrow(() -> new RuntimeException("ویژگی پیدا نشد: " + createDTO.getAttributeId()));
        log.info("Found product: {}, attribute: {}", product, attribute);
        ProductAttributeValue pav = productAttributeValueMapper.toEntity(createDTO);
        pav.setProduct(product);
        pav.setAttribute(attribute);
        log.info("Saving ProductAttributeValue: {}", pav);
        ProductAttributeValue saved = productAttributeValueRepository.save(pav);
        return productAttributeValueMapper.toDTO(saved);
    }
//    @Transactional
//    public ProductAttributeValueDTO create(Long productId, ProductAttributeValueCreateDTO dto) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
//
//        Attribute attribute = attributeRepository.findById(dto.getAttributeId())
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
//
//        ProductAttributeValue pav = new ProductAttributeValue();
//        pav.setProduct(product);
//        pav.setAttribute(attribute);
//
//        setValueWithDefault(pav, dto.getValue());
//
//        return productAttributeValueMapper.toDTO(productAttributeValueRepository.save(pav));
//    }

//    @Transactional
//    public ProductAttributeValueDTO update(Long id, ProductAttributeValueCreateDTO dto) {
//        ProductAttributeValue pav = productAttributeValueRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("ProductAttributeValue not found"));
//
//        setValueWithDefault(pav, dto.getValue());
//
//        return productAttributeValueMapper.toDTO(productAttributeValueRepository.save(pav));
//    }

    /**
     * 📌 متد کمکی برای ست کردن مقدار یا استفاده از پیش‌فرض
     */
    private void setValueWithDefault(ProductAttributeValue pav, String newValue) {
        if (newValue == null || newValue.isBlank()) {
            pav.setValue(pav.getAttribute().getDefaultValue());
        } else {
            pav.setValue(newValue);
        }
    }
//    @Transactional
//    public ProductAttributeValueDTO create(Long productId, ProductAttributeValueCreateDTO dto) {
//        // 📌 بررسی وجود محصول
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
//
//        // 📌 بررسی وجود ویژگی
//        Attribute attribute = attributeRepository.findById(dto.getAttributeId())
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
//
//        // 📌 ساخت Entity و ست کردن مقادیر
//        ProductAttributeValue pav = new ProductAttributeValue();
//        pav.setProduct(product);
//        pav.setAttribute(attribute);
//
//        // 📌 مقداردهی: اگر خالی بود از مقدار پیش‌فرض ویژگی استفاده شود
//        pav.setValue(
//                (dto.getValue() == null || dto.getValue().isBlank())
//                        ? attribute.getDefaultValue()
//                        : dto.getValue()
//        );
//
//        // 📌 ذخیره و تبدیل به DTO
//        ProductAttributeValue saved = productAttributeValueRepository.save(pav);
//        return productAttributeValueMapper.toDTO(saved);
//    }

    /**
     * دریافت همه مقادیر ویژگی‌های یک محصول
     */
    public List<ProductAttributeValueDTO> getByProduct(Long productId) {
        List<ProductAttributeValue> list = productAttributeValueRepository.findByProductId(productId);
        return list.stream()
                .map(productAttributeValueMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * بروزرسانی مقدار ویژگی
     */
    public ProductAttributeValueDTO update(Long id, ProductAttributeValueCreateDTO updateDTO) {
        ProductAttributeValue pav = productAttributeValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("مقدار ویژگی پیدا نشد"));

        Attribute attribute = attributeRepository.findById(updateDTO.getAttributeId())
                .orElseThrow(() -> new RuntimeException("ویژگی پیدا نشد"));

        pav.setAttribute(attribute);
        pav.setValue(updateDTO.getValue());

        ProductAttributeValue updated = productAttributeValueRepository.save(pav);
        return productAttributeValueMapper.toDTO(updated);
    }

    /**
     * حذف مقدار ویژگی
     */
    public void delete(Long id) {
        if (!productAttributeValueRepository.existsById(id)) {
            throw new RuntimeException("مقدار ویژگی پیدا نشد");
        }
        productAttributeValueRepository.deleteById(id);
    }
}

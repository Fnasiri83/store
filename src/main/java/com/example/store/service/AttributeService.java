
        package com.example.store.service;
import com.example.store.dto.AttributeCreateDTO;
import com.example.store.dto.AttributeDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.mapper.AttributeMapper;
import com.example.store.model.Attribute;
import com.example.store.model.Category;
import com.example.store.model.CategoryAttribute;
import com.example.store.repository.AttributeRepository;
import com.example.store.repository.CategoryAttributeRepository;
import com.example.store.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

        /**
 * سرویس برای مدیریت ویژگی‌ها (Attributes) و ویژگی‌های دسته‌بندی (CategoryAttributes).
 * این سرویس عملیات CRUD برای ویژگی‌ها و اتصال ویژگی‌ها به دسته‌بندی‌ها را مدیریت می‌کند.
 */

        @Service
        @RequiredArgsConstructor
        public class AttributeService {
            private final AttributeMapper attributeMapper;
            @Autowired
            private AttributeRepository attributeRepository;
            @Autowired
            private CategoryAttributeRepository categoryAttributeRepository;
            @Autowired
            private CategoryRepository categoryRepository;



            /**
             * افزودن ویژگی به یک دسته‌بندی
             */
            @Transactional
            @CacheEvict(value = "categoryAttributes", allEntries = true)
            public CategoryAttributeDTO addCategoryAttribute(CategoryAttributeCreateDTO dto) {
                CategoryAttribute categoryAttribute = new CategoryAttribute();
                categoryAttribute.setCategory(categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + dto.getCategoryId())));
                categoryAttribute.setAttribute(attributeRepository.findById(dto.getAttributeId())
                        .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + dto.getAttributeId())));
                categoryAttribute.setRequired(dto.isRequired());
                CategoryAttribute saved = categoryAttributeRepository.save(categoryAttribute);
                System.out.println("ویژگی به دسته بندی اضافه شد");
                return new CategoryAttributeDTO(
                        saved.getId(),
                        saved.getCategory().getId(),
                        saved.getAttribute().getId(),
                        saved.getAttribute().getName(),
                        saved.getAttribute().getType(),
                        saved.isRequired(),
                        saved.getCategory().getName(),
                        false // ویژگی جدید مستقیماً به دسته‌بندی اضافه شده
                );
            }

            /**
             * حذف ویژگی از یک دسته‌بندی
             */
            /**
             * حذف ویژگی از یک دسته‌بندی
             */
            @Transactional
            @CacheEvict(value = "categoryAttributes", allEntries = true)  // اضافه: invalidate کش برای همه (ساده)
            public void deleteCategoryAttribute(Long id) {
                if (!categoryAttributeRepository.existsById(id)) {
                    throw new EntityNotFoundException("CategoryAttribute not found");
                }
                categoryAttributeRepository.deleteById(id);
            }


            /**
             * ایجاد یک ویژگی جدید
             */
            @Transactional
            public AttributeDTO create(AttributeCreateDTO createDTO) {
                Attribute attribute = attributeMapper.toEntity(createDTO);
                System.out.println("ویژگی جدید اضافه شد");
                return attributeMapper.toDTO(attributeRepository.save(attribute));
            }
            /**
             * دریافت لیست همه ویژگی‌ها
             */
            public List<AttributeDTO> getAll() {
                return attributeRepository.findAll()
                        .stream()
                        .map(attributeMapper::toDTO)
                        .collect(Collectors.toList());
            }

            /**
             * دریافت یک ویژگی بر اساس ID
             */
            public AttributeDTO getById(Long id) {
                Attribute attribute = attributeRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
                return attributeMapper.toDTO(attribute);
            }

            /**
             * بروزرسانی ویژگی
             */
            @Transactional
            public AttributeDTO update(Long id, AttributeCreateDTO updateDTO) {
                Attribute attribute = attributeRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
                attribute.setName(updateDTO.getName());
                attribute.setType(updateDTO.getType());
                return attributeMapper.toDTO(attributeRepository.save(attribute));
            }

            /**
             * حذف ویژگی
             */
            @Transactional
            public void delete(Long id) {
                if (!attributeRepository.existsById(id)) {
                    throw new EntityNotFoundException("Attribute not found");
                }
                categoryAttributeRepository.deleteByAttributeId(id);
                attributeRepository.deleteById(id);
            }
        }

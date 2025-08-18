
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
             * دریافت ویژگی‌های مرتبط با یک دسته‌بندی و والدهایش (با ارث‌بری).
             * @param categoryId ID دسته‌بندی
             * @return لیست DTOهای ویژگی‌های دسته‌بندی (شامل ویژگی‌های ارث‌بری‌شده)
             */
            @Transactional(readOnly = true)
            @Cacheable(value = "categoryAttributes", key = "#categoryId")
            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
                // دیباگ: چک کردن دسته‌بندی جاری
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
                System.out.println("Current category: ID=" + category.getId() + ", Name=" + category.getName());

                // گرفتن ویژگی‌های مستقیم
                List<CategoryAttribute> directAttributes = categoryAttributeRepository.findByCategoryId(categoryId);
                System.out.println("Direct attributes count: " + directAttributes.size());
                directAttributes.forEach(attr -> System.out.println("Direct attr: ID=" + attr.getId() +
                        ", AttributeID=" + attr.getAttribute().getId() + ", AttributeName=" + attr.getAttribute().getName()));

                // گرفتن ID والدها
                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
                System.out.println("Parent IDs: " + parentIds);

                List<CategoryAttributeDTO> result = new ArrayList<>();
                Set<Long> usedAttributeIds = new HashSet<>();

                // 1. ویژگی‌های مستقیم (اولویت بالاتر)
                String currentCategoryName = category.getName() != null ? category.getName() : "Unknown Category (" + categoryId + ")";
                for (CategoryAttribute attr : directAttributes) {
                    Attribute attribute = attr.getAttribute();
                    usedAttributeIds.add(attribute.getId());
                    result.add(new CategoryAttributeDTO(
                            attr.getId(),
                            category.getId(),
                            attribute.getId(),
                            attribute.getName() != null ? attribute.getName() : "Unknown Attribute (" + attribute.getId() + ")",
                            attribute.getType(),
                            attr.isRequired(),
                            currentCategoryName,
                            false
                    ));
                }

                // 2. ویژگی‌های والدها
                if (!parentIds.isEmpty()) {
                    // لود دسته‌بندی‌های والد
                    List<Category> parents = categoryRepository.findAllById(parentIds);
                    Map<Long, String> parentNames = parents.stream()
                            .collect(Collectors.toMap(
                                    Category::getId,
                                    c -> c.getName() != null ? c.getName() : "Unknown Parent (" + c.getId() + ")"
                            ));
                    System.out.println("Parent names: " + parentNames);

                    List<CategoryAttribute> parentAttributes = categoryAttributeRepository.findByCategoryIdIn(parentIds);
                    System.out.println("Parent attributes count: " + parentAttributes.size());
                    parentAttributes.forEach(attr -> System.out.println("Parent attr: ID=" + attr.getId() +
                            ", CategoryID=" + attr.getCategory().getId() + ", AttributeID=" + attr.getAttribute().getId() +
                            ", AttributeName=" + attr.getAttribute().getName()));

                    for (Long parentId : parentIds) {
                        String parentName = parentNames.getOrDefault(parentId, "Unknown Parent (" + parentId + ")");
                        parentAttributes.stream()
                                .filter(attr -> attr.getCategory().getId().equals(parentId))
                                .forEach(attr -> {
                                    Attribute attribute = attr.getAttribute();
                                    Long attrId = attribute.getId();
                                    if (!usedAttributeIds.contains(attrId)) {
                                        usedAttributeIds.add(attrId);
                                        result.add(new CategoryAttributeDTO(
                                                attr.getId(),
                                                category.getId(),
                                                attrId,
                                                attribute.getName() != null ? attribute.getName() : "Unknown Attribute (" + attrId + ")",
                                                attribute.getType(),
                                                attr.isRequired(),
                                                parentName,
                                                true
                                        ));
                                    }
                                });
                    }
                } else {
                    System.out.println("No parent IDs found for category " + categoryId);
                }

                // دیباگ: نمایش نتیجه نهایی
                System.out.println("Resulting DTOs count: " + result.size());
                result.forEach(dto -> System.out.println("DTO: id=" + dto.getId() +
                        ", categoryId=" + dto.getCategoryId() +
                        ", attributeId=" + dto.getAttributeId() +
                        ", attributeName=" + dto.getAttributeName() +
                        ", inherited=" + dto.isInherited() +
                        ", categoryName=" + dto.getCategoryName()));

                return result;
            }

//            @Transactional(readOnly = true)
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                // گرفتن دسته‌بندی جاری
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
//
//                // گرفتن ویژگی‌های مستقیم
//                List<CategoryAttribute> directAttributes = categoryAttributeRepository.findByCategoryId(categoryId);
//
//                // گرفتن ID والدها (شامل پدربزرگ و بالاتر، با کوئری بازگشتی)
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                List<CategoryAttributeDTO> result = new ArrayList<>();
//                Set<Long> usedAttributeIds = new HashSet<>();
//
//                // 1. ویژگی‌های مستقیم (اولویت بالاتر)
//                for (CategoryAttribute attr : directAttributes) {
//                    usedAttributeIds.add(attr.getAttribute().getId());
//                    result.add(new CategoryAttributeDTO(
//                            attr.getId(),
//                            category.getId(),  // همیشه categoryId جاری
//                            attr.getAttribute().getId(),
//                            attr.getAttribute().getName(),
//                            attr.getAttribute().getType(),
//                            attr.isRequired(),
//                            category.getName(),  // نام منبع: برای مستقیم، نام جاری
//                            false  // مستقیم
//                    ));
//                }
//
//                // 2. ویژگی‌های والدها (از نزدیک‌ترین والد به دورترین، برای اولویت override)
//                if (!parentIds.isEmpty()) {
//                    // لود دسته‌بندی‌های والد برای گرفتن نام
//                    Map<Long, String> parentNames = categoryRepository.findAllById(parentIds)
//                            .stream()
//                            .collect(Collectors.toMap(Category::getId, Category::getName));
//
//                    List<CategoryAttribute> parentAttributes = categoryAttributeRepository.findByCategoryIdIn(parentIds);
//                    for (Long parentId : parentIds) {  // parentIds به ترتیب depth ASC (نزدیک به دور)
//                        String parentName = parentNames.getOrDefault(parentId, "Unknown");  // جلوگیری از null
//                        if (parentName == null) {
//                            continue;
//                        }
//                        parentAttributes.stream()
//                                .filter(attr -> attr.getCategory().getId().equals(parentId))
//                                .forEach(attr -> {
//                                    Long attrId = attr.getAttribute().getId();
//                                    if (!usedAttributeIds.contains(attrId)) {
//                                        usedAttributeIds.add(attrId);
//                                        result.add(new CategoryAttributeDTO(
//                                                attr.getId(),
//                                                category.getId(),  // تغییر: همیشه categoryId جاری (برای consistency در لیست)
//                                                attrId,
//                                                attr.getAttribute().getName(),
//                                                attr.getAttribute().getType(),
//                                                attr.isRequired(),
//                                                parentName,  // نام منبع: نام والد برای نشان دادن ارث‌بری
//                                                true  // ارث‌بری‌شده
//                                        ));
//                                    }
//                                });
//                    }
//                }
//
//                return result;
//            }
//            @Transactional(readOnly = true)
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                // گرفتن دسته‌بندی جاری
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
//
//                // گرفتن ویژگی‌های مستقیم
//                List<CategoryAttribute> directAttributes = categoryAttributeRepository.findByCategoryId(categoryId);
//
//                // گرفتن ID والدها
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                List<CategoryAttributeDTO> result = new ArrayList<>();
//                Set<Long> usedAttributeIds = new HashSet<>();
//
//                // 1. ویژگی‌های مستقیم
//                for (CategoryAttribute attr : directAttributes) {
//                    usedAttributeIds.add(attr.getAttribute().getId());
//                    result.add(new CategoryAttributeDTO(
//                            attr.getId(),
//                            category.getId(),
//                            attr.getAttribute().getId(),
//                            attr.getAttribute().getName(),
//                            attr.getAttribute().getType(),
//                            attr.isRequired(),
//                            category.getName(), // نام دسته‌بندی جاری
//                            false // مستقیم
//                    ));
//                }
//
//                // 2. ویژگی‌های والدها
//                if (!parentIds.isEmpty()) {
//                    // لود دسته‌بندی‌های والد برای گرفتن نام
//                    Map<Long, String> parentNames = categoryRepository.findAllById(parentIds)
//                            .stream()
//                            .collect(Collectors.toMap(Category::getId, Category::getName));
//
//                    List<CategoryAttribute> parentAttributes = categoryAttributeRepository.findByCategoryIdIn(parentIds);
//                    for (Long parentId : parentIds) {
//                        String parentName = parentNames.get(parentId);
//                        if (parentName == null) {
//                            continue; // والد پیدا نشد
//                        }
//                        parentAttributes.stream()
//                                .filter(attr -> attr.getCategory().getId().equals(parentId))
//                                .forEach(attr -> {
//                                    if (!usedAttributeIds.contains(attr.getAttribute().getId())) {
//                                        usedAttributeIds.add(attr.getAttribute().getId());
//                                        result.add(new CategoryAttributeDTO(
//                                                attr.getId(),
//                                                parentId,
//                                                attr.getAttribute().getId(),
//                                                attr.getAttribute().getName(),
//                                                attr.getAttribute().getType(),
//                                                attr.isRequired(),
//                                                parentName, // نام والد
//                                                true // ارث‌بری‌شده
//                                        ));
//                                    }
//                                });
//                    }
//                }
//
//                return result;
//            }
//            @Transactional(readOnly = true)
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                // گرفتن دسته‌بندی جاری
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
//
//                // ویژگی‌های مستقیم دسته‌بندی
//                List<CategoryAttribute> directAttributes = categoryAttributeRepository.findByCategoryId(categoryId);
//
//                // گرفتن ID همه والدها (چندسطحی)
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                List<CategoryAttributeDTO> result = new ArrayList<>();
//                Set<Long> usedAttributeIds = new HashSet<>();
//
//                // ویژگی‌های مستقیم
//                for (CategoryAttribute attr : directAttributes) {
//                    usedAttributeIds.add(attr.getAttribute().getId());
//                    result.add(new CategoryAttributeDTO(
//                            attr.getId(),
//                            category.getId(),
//                            attr.getAttribute().getId(),
//                            attr.getAttribute().getName(),
//                            attr.getAttribute().getType(),
//                            attr.isRequired(),
//                            category.getName(),
//                            false // ویژگی مستقیم
//                    ));
//                }
//
//                // ویژگی‌های والدها
//                if (!parentIds.isEmpty()) {
//                    List<CategoryAttribute> parentAttributes = categoryAttributeRepository.findByCategoryIdIn(parentIds);
//
//                    // والد نزدیک‌تر اول
//                    parentIds.forEach(parentId -> {
//                        parentAttributes.stream()
//                                .filter(attr -> attr.getCategory().getId().equals(parentId))
//                                .forEach(attr -> {
//                                    if (!usedAttributeIds.contains(attr.getAttribute().getId())) {
//                                        usedAttributeIds.add(attr.getAttribute().getId());
//                                        result.add(new CategoryAttributeDTO(
//                                                attr.getId(),
//                                                attr.getCategory().getId(),
//                                                attr.getAttribute().getId(),
//                                                attr.getAttribute().getName(),
//                                                attr.getAttribute().getType(),
//                                                attr.isRequired(),
//                                                attr.getCategory().getName(),
//                                                true // ارث‌بری‌شده
//                                        ));
//                                    }
//                                });
//                    });
//                }
//
//                return result;
//            }

//            @Transactional(readOnly = true)
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                // گرفتن دسته‌بندی جاری
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
//
//                // ویژگی‌های مستقیم دسته‌بندی
//                List<CategoryAttribute> directAttributes = categoryAttributeRepository.findByCategoryId(categoryId);
//
//                // گرفتن ID همه والدها (چندسطحی)
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                List<CategoryAttributeDTO> result = new ArrayList<>();
//
//                // ابتدا ویژگی‌های مستقیم خود دسته
//                Set<Long> directAttributeIds = new HashSet<>();
//                for (CategoryAttribute attr : directAttributes) {
//                    directAttributeIds.add(attr.getAttribute().getId());
//                    result.add(new CategoryAttributeDTO(
//                            attr.getId(),
//                            category.getId(),
//                            attr.getAttribute().getId(),
//                            attr.getAttribute().getName(),
//                            attr.getAttribute().getType(),
//                            attr.isRequired(),
//                            category.getName(),
//                            false // ویژگی مستقیم
//                    ));
//                }
//
//                // سپس ویژگی‌های والدها (که در دسته جاری override نشده‌اند)
//                if (!parentIds.isEmpty()) {
//                    List<CategoryAttribute> parentAttributes = categoryAttributeRepository.findByCategoryIdIn(parentIds);
//                    for (CategoryAttribute attr : parentAttributes) {
//                        if (!directAttributeIds.contains(attr.getAttribute().getId())) {
//                            result.add(new CategoryAttributeDTO(
//                                    attr.getId(),
//                                    attr.getCategory().getId(),
//                                    attr.getAttribute().getId(),
//                                    attr.getAttribute().getName(),
//                                    attr.getAttribute().getType(),
//                                    attr.isRequired(),
//                                    attr.getCategory().getName(),
//                                    true // ارث‌بری‌شده
//                            ));
//                        }
//                    }
//                }
//
//                return result;
//            }
//            @Transactional(readOnly = true)
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                List<CategoryAttributeDTO> attributes = new ArrayList<>();
//
//                // اطمینان از وجود دسته‌بندی
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
//
//                // گرفتن ویژگی‌های خود دسته‌بندی (inherited = false)
//                attributes.addAll(categoryAttributeRepository.findByCategoryId(categoryId)
//                        .stream()
//                        .map(attr -> new CategoryAttributeDTO(
//                                attr.getId(),
//                                attr.getCategory().getId(),
//                                attr.getAttribute().getId(),
//                                attr.getAttribute().getName(),
//                                attr.getAttribute().getType(),
//                                attr.isRequired(),
//                                attr.getCategory().getName(),
//                                false // مستقیماً متعلق به دسته‌بندی
//                        ))
//                        .collect(Collectors.toList()));
//
//                // گرفتن IDهای همه والدها (به جز خود دسته)
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                // گرفتن ویژگی‌های والدها (inherited = true)
//                if (!parentIds.isEmpty()) {
//                    attributes.addAll(categoryAttributeRepository.findByCategoryIdIn(parentIds)
//                            .stream()
//                            .map(attr -> new CategoryAttributeDTO(
//                                    attr.getId(),
//                                    attr.getCategory().getId(),
//                                    attr.getAttribute().getId(),
//                                    attr.getAttribute().getName(),
//                                    attr.getAttribute().getType(),
//                                    attr.isRequired(),
//                                    attr.getCategory().getName(),
//                                    true // ارث‌بری‌شده از والدها
//                            ))
//                            .collect(Collectors.toList()));
//                }
//
//                return attributes;
//            }
//            @Transactional
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                List<CategoryAttributeDTO> attributes = new ArrayList<>();
//
//                // اطمینان از وجود دسته‌بندی
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//
//                // گرفتن IDهای والدها
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                // گرفتن ویژگی‌های خود دسته‌بندی (inherited = false)
//                attributes.addAll(categoryAttributeRepository.findByCategoryId(categoryId)
//                        .stream()
//                        .map(attr -> new CategoryAttributeDTO(
//                                attr.getId(),
//                                attr.getCategory().getId(),
//                                attr.getAttribute().getId(),
//                                attr.getAttribute().getName(),
//                                attr.getAttribute().getType(),
//                                attr.isRequired(),
//                                attr.getCategory().getName(),
//                                false // مستقیماً متعلق به دسته‌بندی
//                        ))
//                        .collect(Collectors.toList()));
//
//                // گرفتن ویژگی‌های والدها (inherited = true)
//                if (!parentIds.isEmpty()) {
//                    attributes.addAll(categoryAttributeRepository.findByCategoryIdIn(parentIds)
//                            .stream()
//                            .map(attr -> new CategoryAttributeDTO(
//                                    attr.getId(),
//                                    attr.getCategory().getId(),
//                                    attr.getAttribute().getId(),
//                                    attr.getAttribute().getName(),
//                                    attr.getAttribute().getType(),
//                                    attr.isRequired(),
//                                    attr.getCategory().getName(),
//                                    true // ارث‌بری‌شده از والدها
//                            ))
//                            .collect(Collectors.toList()));
//                }
//
//                return attributes;
//            }
//            @Transactional
//            @Cacheable(value = "categoryAttributes", key = "#categoryId")
//            public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//                List<CategoryAttributeDTO> attributes = new ArrayList<>();
//
//                // اطمینان از وجود دسته‌بندی
//                Category category = categoryRepository.findById(categoryId)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//
//                // گرفتن IDهای والدها (شامل خود دسته‌بندی)
//                List<Long> parentIds = categoryRepository.findParentIds(categoryId);
//
//                // گرفتن ویژگی‌های تمام دسته‌بندی‌های والد و خود دسته‌بندی
//                attributes.addAll(categoryAttributeRepository.findByCategoryIdIn(parentIds)
//                        .stream()
//                        .map(attr -> new CategoryAttributeDTO(
//                                attr.getId(),
//                                attr.getCategory().getId(),
//                                attr.getAttribute().getId(),
//                                attr.getAttribute().getName(),
//                                attr.getAttribute().getType(),
//                                attr.isRequired(),
//                                attr.getCategory().getName(),
//                                attr.getCategory().getId().equals(categoryId) ? false : true // inherited برای والدها true
//                        ))
//                        .collect(Collectors.toList()));
//
//                return attributes;
//            }

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
//            @Transactional
//            public CategoryAttributeDTO addCategoryAttribute(CategoryAttributeCreateDTO dto) {
//                CategoryAttribute categoryAttribute = new CategoryAttribute();
//                categoryAttribute.setCategory(categoryRepository.findById(dto.getCategoryId())
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found")));
//                categoryAttribute.setAttribute(attributeRepository.findById(dto.getAttributeId())
//                        .orElseThrow(() -> new EntityNotFoundException("Attribute not found")));
//                categoryAttribute.setRequired(dto.isRequired());
//                CategoryAttribute saved = categoryAttributeRepository.save(categoryAttribute);
//                return new CategoryAttributeDTO(
//                        saved.getId(),
//                        saved.getCategory().getId(),
//                        saved.getAttribute().getId(),
//                        saved.getAttribute().getName(),
//                        saved.getAttribute().getType(),
//                        saved.isRequired(),
//                        saved.getCategory().getName(),
//                        false // ویژگی جدید مستقیماً به دسته‌بندی اضافه شده
//                );
//            }

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
//            @Transactional
//            public void deleteCategoryAttribute(Long id) {
//                if (!categoryAttributeRepository.existsById(id)) {
//                    throw new EntityNotFoundException("CategoryAttribute not found");
//                }
//                categoryAttributeRepository.deleteById(id);
//            }

            /**
             * ایجاد یک ویژگی جدید
             */
            @Transactional
            public AttributeDTO create(AttributeCreateDTO createDTO) {
                Attribute attribute = attributeMapper.toEntity(createDTO);
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


//@Service
//@RequiredArgsConstructor
//public class AttributeService {
//    private final AttributeMapper attributeMapper;
//    @Autowired
//    private AttributeRepository attributeRepository;
//    @Autowired
//    private CategoryAttributeRepository categoryAttributeRepository;
//    @Autowired
//    private CategoryRepository categoryRepository;
//    /**
//     * ایجاد یک ویژگی جدید (مثل "رنگ" یا "وزن").
//     * @param createDTO DTO حاوی اطلاعات ویژگی جدید
//     * @return DTO ویژگی ایجاد شده
//     */
//    @Transactional
//    public AttributeDTO create(AttributeCreateDTO createDTO) {
//        Attribute attribute = attributeMapper.toEntity(createDTO);
//        return attributeMapper.toDTO(attributeRepository.save(attribute));
//    }
//    /**
//     * دریافت لیست همه ویژگی‌ها.
//     * @return لیست DTOهای ویژگی‌ها
//     */
//    public List<AttributeDTO> getAll() {
//        return attributeRepository.findAll()
//                .stream()
//                .map(attributeMapper::toDTO)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * دریافت یک ویژگی بر اساس ID.
//     * @param id ID ویژگی
//     * @return DTO ویژگی
//     * @throws EntityNotFoundException اگر ویژگی یافت نشود
//     */
//    public AttributeDTO getById(Long id) {
//        Attribute attribute = attributeRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
//        return attributeMapper.toDTO(attribute);
//    }
//    /**
//     * بروزرسانی ویژگی بر اساس ID.
//     * @param id ID ویژگی
//     * @param updateDTO DTO حاوی اطلاعات بروزرسانی
//     * @return DTO ویژگی بروزرسانی شده
//     * @throws EntityNotFoundException اگر ویژگی یافت نشود
//     */
//    @Transactional
//    public AttributeDTO update(Long id, AttributeCreateDTO updateDTO) {
//        Attribute attribute = attributeRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
//        attribute.setName(updateDTO.getName());
//        attribute.setType(updateDTO.getType());
//        return attributeMapper.toDTO(attributeRepository.save(attribute));
//    }
//    /**
//     * حذف ویژگی بر اساس ID.
//     * ابتدا ردیف‌های مرتبط در جدول category_attributes حذف می‌شوند.
//     * @param id ID ویژگی
//     * @throws EntityNotFoundException اگر ویژگی یافت نشود
//     */
//    @Transactional
//    public void delete(Long id) {
//        if (!attributeRepository.existsById(id)) {
//            throw new EntityNotFoundException("Attribute not found");
//        }
//        // حذف ردیف‌های مرتبط در category_attributes
//        categoryAttributeRepository.deleteByAttributeId(id);
//        attributeRepository.deleteById(id);
//    }
//
//    /**
//     * دریافت ویژگی‌های مرتبط با یک دسته‌بندی (برای CategoryFormComponent).
//     * @param categoryId ID دسته‌بندی
//     * @return لیست DTOهای ویژگی‌های دسته‌بندی
//     */
//    @Transactional
//    public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
//        return categoryAttributeRepository.findByCategoryId(categoryId)
//                .stream()
//                .map(attr -> new CategoryAttributeDTO(
//                        attr.getId(),
//                        attr.getCategory().getId(),
//                        attr.getAttribute().getId(),
//                        attr.getAttribute().getName(),
//                        attr.getAttribute().getType(),
//                        attr.isRequired(),
//                        attr.getCategory().getName()
//                ))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * افزودن ویژگی به یک دسته‌بندی (برای CategoryFormComponent).
//     * @param dto DTO حاوی اطلاعات ویژگی دسته‌بندی
//     * @return DTO ویژگی دسته‌بندی ایجاد شده
//     * @throws EntityNotFoundException اگر دسته‌بندی یا ویژگی یافت نشود
//     */
//    @Transactional
//    public CategoryAttributeDTO addCategoryAttribute(CategoryAttributeCreateDTO dto) {
//        CategoryAttribute categoryAttribute = new CategoryAttribute();
//        categoryAttribute.setCategory(categoryRepository.findById(dto.getCategoryId())
//                .orElseThrow(() -> new EntityNotFoundException("Category not found")));
//        categoryAttribute.setAttribute(attributeRepository.findById(dto.getAttributeId())
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found")));
//        categoryAttribute.setRequired(dto.isRequired());
//        CategoryAttribute saved = categoryAttributeRepository.save(categoryAttribute);
//        return new CategoryAttributeDTO(
//                saved.getId(),
//                saved.getCategory().getId(),
//                saved.getAttribute().getId(),
//                saved.getAttribute().getName(),
//                saved.getAttribute().getType(),
//                saved.isRequired(),
//                saved.getCategory().getName()
//        );
//    }
//
//    /**
//     * حذف ویژگی از یک دسته‌بندی بر اساس ID.
//     * @param id ID ویژگی دسته‌بندی
//     * @throws EntityNotFoundException اگر ویژگی دسته‌بندی یافت نشود
//     */
//    @Transactional
//    public void deleteCategoryAttribute(Long id) {
//        if (!categoryAttributeRepository.existsById(id)) {
//            throw new EntityNotFoundException("CategoryAttribute not found");
//        }
//        categoryAttributeRepository.deleteById(id);
//    }
//}

   //package com.example.store.service;
//
//import com.example.store.dto.AttributeCreateDTO;
//import com.example.store.dto.AttributeDTO;
//import com.example.store.mapper.AttributeMapper;
//import com.example.store.model.Attribute;
//import com.example.store.repository.AttributeRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AttributeService {
//
//    private final AttributeRepository attributeRepository;
//    private final AttributeMapper attributeMapper;
//
//    /**
//     * ایجاد یک ویژگی جدید
//     */
//    public AttributeDTO create(AttributeCreateDTO createDTO) {
//        Attribute attribute = attributeMapper.toEntity(createDTO);
//        return attributeMapper.toDTO(attributeRepository.save(attribute));
//    }
//
//    /**
//     * دریافت لیست همه ویژگی‌ها
//     */
//    public List<AttributeDTO> getAll() {
//        return attributeRepository.findAll()
//                .stream()
//                .map(attributeMapper::toDTO)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * دریافت یک ویژگی بر اساس ID
//     */
//    public AttributeDTO getById(Long id) {
//        Attribute attribute = attributeRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
//        return attributeMapper.toDTO(attribute);
//    }
//
//    /**
//     * بروزرسانی ویژگی
//     */
//    public AttributeDTO update(Long id, AttributeCreateDTO updateDTO) {
//        Attribute attribute = attributeRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
//
//        attribute.setName(updateDTO.getName());
//        attribute.setType(updateDTO.getType());
//
//        return attributeMapper.toDTO(attributeRepository.save(attribute));
//    }
//
//    /**
//     * حذف ویژگی
//     */
//    public void delete(Long id) {
//        if (!attributeRepository.existsById(id)) {
//            throw new EntityNotFoundException("Attribute not found");
//        }
//        attributeRepository.deleteById(id);
//    }
//}

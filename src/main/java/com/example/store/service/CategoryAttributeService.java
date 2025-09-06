package com.example.store.service;

import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.mapper.CategoryAttributeMapper;
import com.example.store.model.Attribute;
import com.example.store.model.Category;
import com.example.store.model.CategoryAttribute;
import com.example.store.repository.CategoryAttributeRepository;
import com.example.store.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryAttributeService {

    private final CategoryAttributeRepository categoryAttributeRepository;
    private final CategoryAttributeMapper categoryAttributeMapper;
    private final CategoryRepository categoryRepository;


    /**
     * اتصال ویژگی به دسته‌بندی
     */
    public CategoryAttributeDTO create(CategoryAttributeCreateDTO dto) {
        CategoryAttribute entity = categoryAttributeMapper.toEntity(dto);
        return categoryAttributeMapper.toDTO(categoryAttributeRepository.save(entity));
    }

    /**
     * دریافت همه ویژگی‌های یک دسته‌بندی
     */
    @Transactional(readOnly = true)
    public List<CategoryAttributeDTO> getByCategoryId(Long categoryId) {
        log.info("=== ورود به getByCategoryId برای categoryId={} ===", categoryId);

        // دسته‌بندی جاری
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        // ویژگی‌های مستقیم دسته‌بندی
        List<CategoryAttribute> directAttributes = categoryAttributeRepository.findByCategoryId(categoryId);

        List<CategoryAttributeDTO> result = new ArrayList<>();
        Set<Long> usedAttributeIds = new HashSet<>();

        // تبدیل ویژگی‌های مستقیم
        for (CategoryAttribute attr : directAttributes) {
            Attribute attribute = attr.getAttribute();
            if (attribute == null) continue;

            usedAttributeIds.add(attribute.getId());
            result.add(new CategoryAttributeDTO(
                    attr.getId(),
                    category.getId(),
                    attribute.getId(),
                    attribute.getName() != null ? attribute.getName() : "ویژگی ناشناخته",
                    attribute.getType(),
                    attr.isRequired(),
                    category.getName(),
                    false // مستقیم از همین دسته‌بندی است
            ));
        }

        // پیدا کردن والدها
        List<Long> parentIds = categoryRepository.findParentIds(categoryId);

        if (!parentIds.isEmpty()) {
            List<Category> parents = categoryRepository.findAllById(parentIds);

            Map<Long, String> parentNames = parents.stream()
                    .collect(Collectors.toMap(Category::getId,
                            c -> c.getName() != null ? c.getName() : "والد ناشناخته"));

            List<CategoryAttribute> parentAttributes = categoryAttributeRepository.findByCategoryIdIn(parentIds);

            // افزودن ویژگی‌های والدها (بدون تکرار)
            for (Long parentId : parentIds) {
                String parentName = parentNames.getOrDefault(parentId, "والد ناشناخته");

                parentAttributes.stream()
                        .filter(attr -> attr.getCategory().getId().equals(parentId))
                        .forEach(attr -> {
                            Attribute attribute = attr.getAttribute();
                            if (attribute == null) return;

                            Long attrId = attribute.getId();
                            if (!usedAttributeIds.contains(attrId)) {
                                usedAttributeIds.add(attrId);
                                result.add(new CategoryAttributeDTO(
                                        attr.getId(),
                                        category.getId(), // همچنان categoryId فرزند رو برمی‌گردونیم
                                        attrId,
                                        attribute.getName() != null ? attribute.getName() : "ویژگی ناشناخته",
                                        attribute.getType(),
                                        attr.isRequired(),
                                        parentName,
                                        true // این inherited است
                                ));
                            }
                        });
            }
        }

        log.info("تعداد کل ویژگی‌ها: {}", result.size());
        return result;
    }


    /**
     * حذف یک ویژگی از دسته‌بندی
     */
    public void delete(Long id) {
        if (!categoryAttributeRepository.existsById(id)) {
            throw new EntityNotFoundException("CategoryAttribute not found");
        }
        categoryAttributeRepository.deleteById(id);
    }
}


//    public List<CategoryAttributeDTO> getByCategoryId(Long categoryId) {
//        return categoryAttributeRepository.findByCategoryId(categoryId)
//                .stream()
//                .map(categoryAttributeMapper::toDTO)
//                .collect(Collectors.toList());
//    }

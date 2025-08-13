
        package com.example.store.service;
import com.example.store.dto.AttributeCreateDTO;
import com.example.store.dto.AttributeDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.mapper.AttributeMapper;
import com.example.store.model.Attribute;
import com.example.store.model.CategoryAttribute;
import com.example.store.repository.AttributeRepository;
import com.example.store.repository.CategoryAttributeRepository;
import com.example.store.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
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
     * ایجاد یک ویژگی جدید (مثل "رنگ" یا "وزن").
     * @param createDTO DTO حاوی اطلاعات ویژگی جدید
     * @return DTO ویژگی ایجاد شده
     */
    @Transactional
    public AttributeDTO create(AttributeCreateDTO createDTO) {
        Attribute attribute = attributeMapper.toEntity(createDTO);
        return attributeMapper.toDTO(attributeRepository.save(attribute));
    }
    /**
     * دریافت لیست همه ویژگی‌ها.
     * @return لیست DTOهای ویژگی‌ها
     */
    public List<AttributeDTO> getAll() {
        return attributeRepository.findAll()
                .stream()
                .map(attributeMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * دریافت یک ویژگی بر اساس ID.
     * @param id ID ویژگی
     * @return DTO ویژگی
     * @throws EntityNotFoundException اگر ویژگی یافت نشود
     */
    public AttributeDTO getById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found"));
        return attributeMapper.toDTO(attribute);
    }
    /**
     * بروزرسانی ویژگی بر اساس ID.
     * @param id ID ویژگی
     * @param updateDTO DTO حاوی اطلاعات بروزرسانی
     * @return DTO ویژگی بروزرسانی شده
     * @throws EntityNotFoundException اگر ویژگی یافت نشود
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
     * حذف ویژگی بر اساس ID.
     * ابتدا ردیف‌های مرتبط در جدول category_attributes حذف می‌شوند.
     * @param id ID ویژگی
     * @throws EntityNotFoundException اگر ویژگی یافت نشود
     */
    @Transactional
    public void delete(Long id) {
        if (!attributeRepository.existsById(id)) {
            throw new EntityNotFoundException("Attribute not found");
        }
        // حذف ردیف‌های مرتبط در category_attributes
        categoryAttributeRepository.deleteByAttributeId(id);
        attributeRepository.deleteById(id);
    }

    /**
     * دریافت ویژگی‌های مرتبط با یک دسته‌بندی (برای CategoryFormComponent).
     * @param categoryId ID دسته‌بندی
     * @return لیست DTOهای ویژگی‌های دسته‌بندی
     */
    @Transactional
    public List<CategoryAttributeDTO> getCategoryAttributes(Long categoryId) {
        return categoryAttributeRepository.findByCategoryId(categoryId)
                .stream()
                .map(attr -> new CategoryAttributeDTO(
                        attr.getId(),
                        attr.getCategory().getId(),
                        attr.getAttribute().getId(),
                        attr.getAttribute().getName(),
                        attr.getAttribute().getType(),
                        attr.isRequired(),
                        attr.getCategory().getName()
                ))
                .collect(Collectors.toList());
    }

    /**
     * افزودن ویژگی به یک دسته‌بندی (برای CategoryFormComponent).
     * @param dto DTO حاوی اطلاعات ویژگی دسته‌بندی
     * @return DTO ویژگی دسته‌بندی ایجاد شده
     * @throws EntityNotFoundException اگر دسته‌بندی یا ویژگی یافت نشود
     */
    @Transactional
    public CategoryAttributeDTO addCategoryAttribute(CategoryAttributeCreateDTO dto) {
        CategoryAttribute categoryAttribute = new CategoryAttribute();
        categoryAttribute.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found")));
        categoryAttribute.setAttribute(attributeRepository.findById(dto.getAttributeId())
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found")));
        categoryAttribute.setRequired(dto.isRequired());
        CategoryAttribute saved = categoryAttributeRepository.save(categoryAttribute);
        return new CategoryAttributeDTO(
                saved.getId(),
                saved.getCategory().getId(),
                saved.getAttribute().getId(),
                saved.getAttribute().getName(),
                saved.getAttribute().getType(),
                saved.isRequired(),
                saved.getCategory().getName()
        );
    }

    /**
     * حذف ویژگی از یک دسته‌بندی بر اساس ID.
     * @param id ID ویژگی دسته‌بندی
     * @throws EntityNotFoundException اگر ویژگی دسته‌بندی یافت نشود
     */
    @Transactional
    public void deleteCategoryAttribute(Long id) {
        if (!categoryAttributeRepository.existsById(id)) {
            throw new EntityNotFoundException("CategoryAttribute not found");
        }
        categoryAttributeRepository.deleteById(id);
    }
}

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

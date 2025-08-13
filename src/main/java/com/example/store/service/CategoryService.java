package com.example.store.service;

import com.example.store.dto.CategoryDTO;
import com.example.store.dto.CategoryTreeNodeDTO;
import com.example.store.mapper.CategoryMapper;
import com.example.store.model.Category;
import com.example.store.model.CategoryAttribute;
import com.example.store.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {


    private final CategoryMapper categoryMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private ProductAttributeValueRepository productAttributeValueRepository;
    @Autowired
    private ProductRepository productRepository;
    /**
     * ایجاد دسته‌بندی جدید
     */
    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category savedCategory = categoryRepository.save(category);
        if (savedCategory.getId() == null) {
            throw new RuntimeException("Failed to generate category ID");
        }
        return categoryMapper.toDto(savedCategory);
    }
//    public CategoryDTO create(CategoryDTO dto) {
//        Category category = categoryMapper.toEntity(dto);
//        return categoryMapper.toDto(categoryRepository.save(category));
//    }
//    public CategoryDTO create(CategoryDTO dto) {
//        Category category = categoryMapper.toEntity(dto);
//
//        // بررسی و ست کردن والد ذخیره‌شده
//        if (dto.getParentId() != null) {
//            Category parent = categoryRepository.findById(dto.getParentId())
//                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
//            category.setParent(parent);
//        } else {
//            category.setParent(null);
//        }
//
//        return categoryMapper.toDto(categoryRepository.save(category));
//    }


    /**
     * لیست همه دسته‌ها
     */
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

//    /**
//     * برگرداندن دسته‌بندی به شکل درختی
//     */
//    public List<CategoryTreeNodeDTO> getTree() {
//        List<Category> roots = categoryRepository.findByParentIsNull();
//        return categoryMapper.toTreeNodeList(roots);
//    }

    /**
     * برگرداندن کل دسته‌بندی‌ها به شکل درختی
     * این متد:
     *   1. تمام دسته‌های ریشه (Parent = null) را پیدا می‌کند.
     *   2. برای هر دسته، ساختار زیرمجموعه‌ها را به صورت بازگشتی پر می‌کند.
     *   3. آن را به DTO مخصوص نمایش در PrimeNG تبدیل می‌کند.
     */
    public List<CategoryTreeNodeDTO> getTree() {
        // 1️⃣ پیدا کردن دسته‌های ریشه
        List<Category> rootCategories = categoryRepository.findByParentIsNull();

        // 2️⃣ تبدیل دسته‌های ریشه به DTOهای درختی
        return rootCategories.stream()
                .map(this::buildCategoryTree) // بازگشتی ساختن درخت
                .collect(Collectors.toList());
    }

    /**
     * ساخت یک گره درختی به همراه تمام زیرمجموعه‌های آن به صورت بازگشتی
     * @param category موجودیت دسته
     * @return DTO به شکل درخت
     */
    private CategoryTreeNodeDTO buildCategoryTree(Category category) {
        // تبدیل دسته اصلی به DTO (اطلاعات پایه)
        CategoryTreeNodeDTO node = categoryMapper.toTreeNode(category);

        // اضافه کردن زیرمجموعه‌ها (بازگشتی)
        List<CategoryTreeNodeDTO> childrenNodes = category.getChildren().stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());

        node.setChildren(childrenNodes);

        return node;
    }
    /**
     * دریافت دسته‌بندی بر اساس ID
     */
    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return categoryMapper.toDto(category);
    }

    /**
     * بروزرسانی دسته‌بندی
     */

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(updatedCategory);
    }

//    public CategoryDTO update(Long id, CategoryDTO dto) {
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//
//        category.setName(dto.getName());
//        category.setDescription(dto.getDescription());
//        return categoryMapper.toDto(categoryRepository.save(category));
//    }

    /**
     * حذف دسته‌بندی
     */

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found");
        }
        deleteRecursively(id);
    }
    @Transactional
    public void deleteRecursively(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        // چک وجود محصولات مرتبط
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Cannot delete category because it has associated products");
        }

        // 1️⃣ حذف بازگشتی زیرمجموعه‌ها
        for (Category child : new ArrayList<>(category.getChildren())) {
            deleteRecursively(child.getId());
        }

        // 2️⃣ پیدا کردن و حذف ویژگی‌های مرتبط
        List<CategoryAttribute> categoryAttributes = categoryAttributeRepository.findByCategoryId(id);
        for (CategoryAttribute ca : categoryAttributes) {
            Long attrId = ca.getAttribute().getId();

            // حذف اتصال دسته به ویژگی
            categoryAttributeRepository.delete(ca);

            // بررسی استفاده ویژگی در دسته‌های دیگر
            boolean usedInOtherCategories = !categoryAttributeRepository.findByAttributeId(attrId).isEmpty();
            if (!usedInOtherCategories) {
                // حذف مقادیر ویژگی در محصولات
                productAttributeValueRepository.deleteByAttributeId(attrId);
                // حذف خود ویژگی
                attributeRepository.deleteById(attrId);
            }
        }

        // 3️⃣ حذف دسته‌بندی
        categoryRepository.deleteById(id);
    }
    }
//    @Transactional
//    public void deleteRecursively(Long id) {
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//
//        // 1️⃣ حذف بازگشتی زیرمجموعه‌ها
//        for (Category child : new ArrayList<>(category.getChildren())) {
//            deleteRecursively(child.getId());
//        }
//
//        // 2️⃣ پیدا کردن همه ویژگی‌های این دسته
//        List<CategoryAttribute> categoryAttributes = categoryAttributeRepository.findByCategoryId(id);
//
//        for (CategoryAttribute ca : categoryAttributes) {
//            Long attrId = ca.getAttribute().getId();
//
//            // حذف اتصال دسته به ویژگی
//            categoryAttributeRepository.delete(ca);
//
//            // بررسی اینکه آیا این ویژگی در دسته دیگری استفاده می‌شود یا نه
//            boolean usedInOtherCategories = !categoryAttributeRepository.findByAttributeId(attrId).isEmpty();
//
//            if (!usedInOtherCategories) {
//                // اول همه مقادیر این ویژگی را در محصولات حذف کن
//                productAttributeValueRepository.deleteByAttributeId(attrId);
//
//                // سپس خود ویژگی را حذف کن
//                attributeRepository.deleteById(attrId);
//            }
//        }
//
//        // 3️⃣ حذف دسته‌بندی
//        categoryRepository.deleteById(id);
//    }
//    }
//    private void deleteRecursively(Long id) {
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//
//        // حذف بازگشتی زیرمجموعه‌ها
//        for (Category child : category.getChildren()) {
//            deleteRecursively(child.getId());
//        }
//
//        // حذف ویژگی‌های مرتبط
//        categoryAttributeRepository.deleteByCategory(category);
//
//        // حذف دسته‌بندی
//        categoryRepository.deleteById(id);
//    }
//}

//    public void delete(Long id) {
//        if (!categoryRepository.existsById(id)) {
//            throw new EntityNotFoundException("Category not found");
//        }
//        categoryRepository.deleteById(id);
//    }


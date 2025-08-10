package com.example.store.service;

import com.example.store.dto.CategoryDTO;
import com.example.store.dto.CategoryTreeNodeDTO;
import com.example.store.mapper.CategoryMapper;
import com.example.store.model.Category;
import com.example.store.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * ایجاد دسته‌بندی جدید
     */
    public CategoryDTO create(CategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

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
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    /**
     * حذف دسته‌بندی
     */
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}

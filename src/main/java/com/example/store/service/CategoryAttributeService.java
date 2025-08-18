package com.example.store.service;

import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.mapper.CategoryAttributeMapper;
import com.example.store.model.CategoryAttribute;
import com.example.store.repository.CategoryAttributeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryAttributeService {

    private final CategoryAttributeRepository categoryAttributeRepository;
    private final CategoryAttributeMapper categoryAttributeMapper;


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
    public List<CategoryAttributeDTO> getByCategoryId(Long categoryId) {
        return categoryAttributeRepository.findByCategoryId(categoryId)
                .stream()
                .map(categoryAttributeMapper::toDTO)
                .collect(Collectors.toList());
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

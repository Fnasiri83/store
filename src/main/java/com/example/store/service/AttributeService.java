package com.example.store.service;

import com.example.store.dto.AttributeCreateDTO;
import com.example.store.dto.AttributeDTO;
import com.example.store.mapper.AttributeMapper;
import com.example.store.model.Attribute;
import com.example.store.repository.AttributeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    /**
     * ایجاد یک ویژگی جدید
     */
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
    public void delete(Long id) {
        if (!attributeRepository.existsById(id)) {
            throw new EntityNotFoundException("Attribute not found");
        }
        attributeRepository.deleteById(id);
    }
}

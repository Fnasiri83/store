package com.example.store.mapper;
import com.example.store.dto.CategoryAttributeCreateDTO;
import com.example.store.dto.CategoryAttributeDTO;
import com.example.store.model.CategoryAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryAttributeMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "attribute.id", target = "attributeId")
    @Mapping(source = "attribute.name", target = "attributeName")
    @Mapping(source = "attribute.type", target = "attributeType")
    CategoryAttributeDTO toDTO(CategoryAttribute entity);
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "attributeId", target = "attribute.id")
    CategoryAttribute toEntity(CategoryAttributeCreateDTO createDTO);
}

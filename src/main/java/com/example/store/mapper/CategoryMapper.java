package com.example.store.mapper;

import com.example.store.dto.CategoryDTO;
import com.example.store.dto.CategoryTreeNodeDTO;
import com.example.store.dto.ProductAttributeValueDTO;
import com.example.store.model.Category;
import com.example.store.model.ProductAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "parent.id", target = "parentId")
    CategoryDTO toDto(Category category);

    @Mapping(source = "parentId", target = "parent.id")
    Category toEntity(CategoryDTO dto);

    @Mapping(target = "key", expression = "java(String.valueOf(category.getId()))")
    @Mapping(target = "label", source = "name")
    @Mapping(target = "data", expression = "java(createDataMap(category))")
    @Mapping(target = "attributes", ignore = true)
    CategoryTreeNodeDTO toTreeNode(Category category);

    default List<CategoryTreeNodeDTO> toTreeNodeList(List<Category> categories) {
        if (categories == null) return Collections.emptyList();
        return categories.stream()
                .map(cat -> {
                    CategoryTreeNodeDTO node = toTreeNode(cat);
                    node.setChildren(toTreeNodeList(cat.getChildren()));
                    return node;
                })
                .collect(Collectors.toList());
    }
    // در ProductAttributeValueMapper
    List<ProductAttributeValueDTO> toDTOList(List<ProductAttributeValue> entities);
    default Map<String, Object> createDataMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put("description", category.getDescription() != null ? category.getDescription() : "");
        map.put("id", category.getId());
        return map;
    }
}
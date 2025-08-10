package com.example.store.mapper;

import com.example.store.dto.ProductAttributeValueCreateDTO;
import com.example.store.dto.ProductAttributeValueDTO;
import com.example.store.model.ProductAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductAttributeValueMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "attribute.id", target = "attributeId")
    @Mapping(source = "attribute.name", target = "attributeName")
    @Mapping(source = "attribute.type", target = "attributeType")
    ProductAttributeValueDTO toDTO(ProductAttributeValue entity);
    @Mapping(source = "attributeId", target = "attribute.id")
    ProductAttributeValue toEntity(ProductAttributeValueCreateDTO createDTO);}

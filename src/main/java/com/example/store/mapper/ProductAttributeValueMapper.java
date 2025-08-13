package com.example.store.mapper;

import com.example.store.dto.ProductAttributeValueCreateDTO;
import com.example.store.dto.ProductAttributeValueDTO;
import com.example.store.model.ProductAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductAttributeValueMapper {

    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "attributeType", source = "attribute.type")
    ProductAttributeValueDTO toDTO(ProductAttributeValue pav);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "attribute.id", source = "attributeId") // اصلاح مپینگ

//    @Mapping(target = "attribute", ignore = true)
    ProductAttributeValue toEntity(ProductAttributeValueCreateDTO dto);
}




//package com.example.store.mapper;
//
//import com.example.store.dto.ProductAttributeValueCreateDTO;
//import com.example.store.dto.ProductAttributeValueDTO;
//import com.example.store.model.ProductAttributeValue;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface ProductAttributeValueMapper {
//    @Mapping(source = "product.id", target = "productId")
//    @Mapping(source = "attribute.id", target = "attributeId")
//    @Mapping(source = "attribute.name", target = "attributeName")
//    @Mapping(source = "attribute.type", target = "attributeType")
//    ProductAttributeValueDTO toDTO(ProductAttributeValue entity);
//    @Mapping(source = "attributeId", target = "attribute.id")
//    ProductAttributeValue toEntity(ProductAttributeValueCreateDTO createDTO);}

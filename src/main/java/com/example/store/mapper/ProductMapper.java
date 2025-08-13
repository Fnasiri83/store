package com.example.store.mapper;

import com.example.store.dto.ProductCreateDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring", uses = { ProductAttributeValueMapper.class })
public interface ProductMapper {
    @Mapping(target = "category.id", source = "categoryId")
    Product toEntity(ProductCreateDTO dto);
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductDTO toDTO(Product product);
}
//package com.example.store.mapper;
//
//import com.example.store.dto.ProductCreateDTO;
//import com.example.store.dto.ProductDTO;
//import com.example.store.model.Product;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring", uses = {ProductAttributeValueMapper.class})
//public interface ProductMapper {
//    @Mapping(source = "category.id", target = "categoryId")
//    @Mapping(source = "category.name", target = "categoryName")
//    ProductDTO toDTO(Product product);
//    @Mapping(source = "categoryId", target = "category.id")
//    Product toEntity(ProductCreateDTO dto);}
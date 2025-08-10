package com.example.store.mapper;

import com.example.store.dto.AttributeCreateDTO;
import com.example.store.dto.AttributeDTO;
import com.example.store.model.Attribute;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeMapper {
    AttributeDTO toDTO(Attribute attribute);
    Attribute toEntity(AttributeDTO dto);
    Attribute toEntity(AttributeCreateDTO createDTO);
}
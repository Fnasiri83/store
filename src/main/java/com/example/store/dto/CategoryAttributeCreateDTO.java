package com.example.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributeCreateDTO {
    @NotNull
    private Long categoryId;
    @NotNull
    private Long attributeId;
    private boolean required;}

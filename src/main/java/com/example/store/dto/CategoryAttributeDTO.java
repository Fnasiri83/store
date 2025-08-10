package com.example.store.dto;

import com.example.store.model.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributeDTO {
    private Long id;
    @NotNull
    private Long categoryId;
    @NotNull
    private Long attributeId;
    private String attributeName; // برای نمایش نام ویژگی
    private AttributeType attributeType; // برای نمایش نوع ویژگی
    private boolean required;
    private String categoryName;}

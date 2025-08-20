package com.example.store.dto;

import com.example.store.model.ProductCondition;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    @NotNull
    @PositiveOrZero
    private int stock;
    @NotNull
    private Long categoryId;
    private List<ProductAttributeValueCreateDTO> attributeValues = new ArrayList<>();
//    private ProductCondition condition;
//    private String photoUrl;
}

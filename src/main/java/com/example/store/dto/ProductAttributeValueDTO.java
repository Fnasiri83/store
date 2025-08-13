package com.example.store.dto;

import com.example.store.model.AttributeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValueDTO {
    private Long id;
    private Long attributeId;
    private String attributeName; // برای نمایش
    private AttributeType attributeType; // برای نمایش
    private String value;
    private Long productId;
}

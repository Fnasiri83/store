package com.example.store.dto;

import com.example.store.model.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AttributeDTO {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private AttributeType type;
    private String defaultValue;
//    private List<String> options;
}
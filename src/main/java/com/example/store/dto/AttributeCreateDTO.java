package com.example.store.dto;

import com.example.store.model.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeCreateDTO {
    @NotBlank
    private String name;
    @NotNull
    private AttributeType type;
//    private List<String> options;
}

package com.example.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeNodeDTO {
    private String key; // برای PrimeNG (معادل id)
    private String label; // برای PrimeNG (معادل name)
    private Map<String, Object> data; // برای داده‌های اضافی مثل description
    private List<CategoryTreeNodeDTO> children = new ArrayList<>(); // برای زیرمجموعه‌ها
    private List<CategoryAttributeDTO> attributes = new ArrayList<>();}
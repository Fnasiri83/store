package com.example.store.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    @Enumerated(EnumType.STRING)
    private AttributeType type; // STRING, NUMBER, BOOLEAN, SELECT, MULTISELECT
    // 🔹 مقدار پیش‌فرض برای ویژگی
    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
//    @ElementCollection
//    @CollectionTable(name = "attribute_options", joinColumns = @JoinColumn(name = "attribute_id"))
//    @Column(name = "option")
//    private List<String> options = new ArrayList<>(); // برای SELECT/MULTISELECT
}

package com.example.store.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_attribute_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
//    @NotNull
//    @ManyToOne
//    @JoinColumn(name = "attribute_id")
//    private Attribute attribute;


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
//    @Column(columnDefinition = "TEXT") // برای ذخیره JSON
    private String value; // همچنان String برای سریال‌سازی آرایه
}

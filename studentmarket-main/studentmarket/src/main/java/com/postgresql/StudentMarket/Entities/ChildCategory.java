package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity 
@Table(name = "child_categories")
public class ChildCategory {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "child_id")
    private Integer childId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private ParentCategory parent;

    private String name;
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==== Getters & Setters ====
    public Integer getChildId() {
        return childId;
    }
    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    public ParentCategory getParent() {
        return parent;
    }
    public void setParent(ParentCategory parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

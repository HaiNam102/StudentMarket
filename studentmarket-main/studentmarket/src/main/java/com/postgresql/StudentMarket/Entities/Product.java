// src/main/java/com/postgresql/StudentMarket/Entities/Product.java
package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId; // LONG

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    // ✅ Thêm parent_id (theo yêu cầu)
    @Column(name = "parent_id", nullable = false)
    private Integer parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private ChildCategory childCategory;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    // DB: decimal(38,2)
    @Column(precision = 38, scale = 2)
    private BigDecimal price;

    @Column(length = 20)
    private String status;

    private String type;

    // ✅ Đặt tên cột rõ ràng để khớp DB
    @Column(name = "image_url")
    private String imageUrl;

    private String location;

    @Column(name = "is_hot")
    private Boolean isHot;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;

    // ... các import/annotation khác
    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    @Transient
    public String getRelativeTime() {
    if (createdAt == null) return "";
    long days = java.time.Duration.between(createdAt, java.time.LocalDateTime.now()).toDays();
    if (days == 0) return "Hôm nay";
    if (days == 1) return "1 ngày trước";
    return days + " ngày trước";
}


}

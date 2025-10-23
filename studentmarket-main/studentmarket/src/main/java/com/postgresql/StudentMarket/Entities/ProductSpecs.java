// src/main/java/com/postgresql/StudentMarket/Entities/ProductSpecs.java
package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecs {

    @Id
    @Column(name = "product_id")
    private Long productId; // <-- LONG, KHÔNG @GeneratedValue

    // Shared PK 1-1 với Product
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 100)
    private String origin;

    @Column(length = 100)
    private String material;

    @Column(length = 50)
    private String color;

    @Lob
    private String accessories;
}

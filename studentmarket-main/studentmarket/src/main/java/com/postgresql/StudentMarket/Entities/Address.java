package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses",
       uniqueConstraints = {
         @UniqueConstraint(name = "uk_addresses_product", columnNames = "product_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    private String province;
    private String ward;

    @Column(name = "address_detail")
    private String addressDetail;

    // ✅ 1–1 với Product (FK nằm ở bảng addresses)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // cột bạn đã thêm
    private Product product;

    // ✅ N–1 với User (một user có thể có nhiều địa chỉ)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

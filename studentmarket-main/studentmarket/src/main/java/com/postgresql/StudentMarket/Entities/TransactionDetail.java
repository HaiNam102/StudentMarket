package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transaction_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetail {

    @EmbeddedId
    private TransactionDetailId id = new TransactionDetailId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("transactionId")  // liên kết khóa chính kép
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    // Nếu bạn muốn tính subtotal tự động trong Java (thay vì MySQL GENERATED):
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (quantity != null && pricePerUnit != null) {
            subtotal = pricePerUnit.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

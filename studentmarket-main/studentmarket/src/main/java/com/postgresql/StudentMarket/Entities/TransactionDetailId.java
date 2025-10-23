package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailId implements Serializable {

    private Integer transactionId;
    private Long productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionDetailId)) return false;
        TransactionDetailId that = (TransactionDetailId) o;
        return Objects.equals(transactionId, that.transactionId) &&
               Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, productId);
    }
}

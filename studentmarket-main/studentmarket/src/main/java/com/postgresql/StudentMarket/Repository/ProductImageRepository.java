// ProductImageRepository.java
package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProduct_ProductIdOrderByDisplayOrderAsc(Long productId);
}

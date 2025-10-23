package com.postgresql.StudentMarket.Utils;

import com.postgresql.StudentMarket.Dto.ProductDTO;
import com.postgresql.StudentMarket.Entities.Product;

public class ProductMapper {
    
    public static ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        return new ProductDTO(
            product.getProductId(),
            product.getUserId(),
            product.getName(),
            product.getDescription(),
            product.getType(),
            product.getPrice(),
            product.getStatus(),
            product.getCreatedAt(),
            product.getUpdatedAt(),
            product.getExpirationDate(),
            product.getImageUrl(),
            product.getLocation(),
            product.getIsHot()
        );
    }
}

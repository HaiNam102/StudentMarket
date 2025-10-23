package com.postgresql.StudentMarket.Dto;

import com.postgresql.StudentMarket.Entities.Product;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductViewDTO {
    Long productId;
    String name;
    String shortDescription;
    BigDecimal price;
    String location;
    String imageUrl;
    String relativeTime;
    Boolean isHot;
    String status;

    public static ProductViewDTO fromEntityToDto(Product product) {
        if (product == null) return null;
        return ProductViewDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .shortDescription(shorten(product.getDescription()))
                .price(product.getPrice())
                .location(product.getLocation())
                .imageUrl(product.getImageUrl())
                .relativeTime(product.getRelativeTime())
                .isHot(product.getIsHot())
                .status(product.getStatus())
                .build();
    }

    private static String shorten(String desc) {
        if (desc == null) return "";
        int limit = 60;
        return (desc.length() > limit) ? desc.substring(0, limit - 3) + "..." : desc;
    }
}

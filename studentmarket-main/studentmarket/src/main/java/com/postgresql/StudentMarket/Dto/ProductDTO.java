package com.postgresql.StudentMarket.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTO {
    private Long productId;
    private Integer userId;
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expirationDate;
    private String imageUrl;
    private String location;
    private Boolean isHot;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(Long productId, Integer userId, String name, String description, 
                     String type, BigDecimal price, String status, LocalDateTime createdAt, 
                     LocalDateTime updatedAt, LocalDateTime expirationDate, String imageUrl, 
                     String location, Boolean isHot) {
        this.productId = productId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
        this.location = location;
        this.isHot = isHot;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
}

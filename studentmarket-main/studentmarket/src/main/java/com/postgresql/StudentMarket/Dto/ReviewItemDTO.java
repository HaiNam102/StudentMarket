package com.postgresql.StudentMarket.Dto;

import lombok.*;
import java.time.LocalDateTime;
import com.postgresql.StudentMarket.Entities.Review;
import java.util.Optional;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ReviewItemDTO {
    private Integer reviewId;
    private Integer transactionId;

    private Integer reviewerId;
    private String  reviewerName;
    private String  reviewerAvatar;

    private Integer revieweeId;
    private String  revieweeName;
    private String  revieweeAvatar;   // ⬅️ THÊM DÒNG NÀY

    private Integer rating;
    private String  comment;
    private LocalDateTime createdAt;
    private String  role;

    public static ReviewItemDTO of(Review r) {
        return ReviewItemDTO.builder()
                .reviewId(r.getReviewId())
                .transactionId(r.getTransaction().getTransactionId())
                .reviewerId(r.getReviewer().getUserId())
                .reviewerName(
                    java.util.Optional.ofNullable(r.getReviewer().getFullName()).orElse("Người dùng"))
                .reviewerAvatar(r.getReviewer().getPicture())
                .revieweeId(r.getReviewee().getUserId())
                .revieweeName(
                    java.util.Optional.ofNullable(r.getReviewee().getFullName()).orElse("Người dùng"))
                .revieweeAvatar(r.getReviewee().getPicture())  // ⬅️ THÊM DÒNG NÀY
                .rating(r.getRating())
                .comment(java.util.Optional.ofNullable(r.getComment()).orElse(""))
                .createdAt(r.getCreatedAt())
                .build();
    }
}
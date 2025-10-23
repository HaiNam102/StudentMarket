package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Dto.ReviewItemDTO;
import com.postgresql.StudentMarket.Entities.Review;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepo;

    public Map<String, List<ReviewItemDTO>> getCategorized(Integer meUserId) {
        var asSeller = reviewRepo.findReceivedWhenImSeller(meUserId)
                .stream().map(r -> toDTO(r, "SELLER")).toList();

        var asBuyer = reviewRepo.findReceivedWhenImBuyer(meUserId)
                .stream().map(r -> toDTO(r, "BUYER")).toList();

        var written = reviewRepo.findWrittenByMe(meUserId)
                .stream().map(r -> toDTO(r, "WRITTEN")).toList();

        return Map.of(
                "asSellerReceived", asSeller,   // Tôi là SELLER -> người khác (buyer) đánh giá tôi
                "asBuyerReceived",  asBuyer,    // Tôi là BUYER  -> người khác (seller) đánh giá tôi
                "writtenByMe",      written     // Tôi đã đánh giá người khác
        );
    }

    /**
     * Trả về danh sách transactionId mà user này đã đánh giá (trong tập txIds truyền vào).
     * Dùng cho FE ẩn nút "Đánh giá" nếu giao dịch đã được tôi đánh giá.
     */
    public List<Integer> findReviewedTxIdsByMe(Integer userId, List<Integer> txIds) {
        if (txIds == null || txIds.isEmpty()) return List.of();
        // Dùng luôn query đã viết trong repository để hiệu quả hơn
        return reviewRepo.findReviewedTxIdsByMe(userId, txIds);
    }

    // ================== helpers ==================

    private ReviewItemDTO toDTO(Review r, String role) {
        var txId     = (r.getTransaction() != null) ? r.getTransaction().getTransactionId() : null;
        var reviewer = r.getReviewer();
        var reviewee = r.getReviewee();

        Integer reviewerId = (reviewer != null) ? reviewer.getUserId() : null;
        Integer revieweeId = (reviewee != null) ? reviewee.getUserId() : null;

        String reviewerName   = safeName(reviewer);
        String revieweeName   = safeName(reviewee);
        String reviewerAvatar = avatarOf(reviewer);
        String revieweeAvatar = avatarOf(reviewee);

        LocalDateTime createdAt = (r.getCreatedAt() != null) ? r.getCreatedAt() : LocalDateTime.now();

        return ReviewItemDTO.builder()
    .reviewId(r.getReviewId())
    .transactionId(txId)
    .reviewerId(reviewerId)
    .reviewerName(reviewerName)
    .reviewerAvatar(reviewerAvatar)
    .revieweeId(revieweeId)
    .revieweeName(revieweeName)
    .revieweeAvatar(revieweeAvatar)   // ⬅️ THÊM DÒNG NÀY
    .rating(r.getRating())
    .comment(r.getComment() != null ? r.getComment() : "")
    .createdAt(createdAt)
    .role(role != null ? role.toUpperCase() : null)
    .build();
    }

    private String safeName(User u) {
        if (u == null) return "Người dùng";
        try {
            var m1 = u.getClass().getMethod("getFullName");
            Object v1 = m1.invoke(u);
            if (v1 != null && !v1.toString().isBlank()) return v1.toString();
        } catch (Exception ignore) { /* no-op */ }

        try {
            var m2 = u.getClass().getMethod("getEmail");
            Object v2 = m2.invoke(u);
            if (v2 != null && !v2.toString().isBlank()) return v2.toString();
        } catch (Exception ignore) { /* no-op */ }

        return "Người dùng";
    }

    private String avatarOf(User u) {
        if (u == null) return "/image/header/profile-user.png";
        try {
            var m = u.getClass().getMethod("getAvatarUrl");
            Object v = m.invoke(u);
            if (v != null && !v.toString().isBlank()) return v.toString();
        } catch (Exception ignore) { /* thử tên method khác */ }

        try {
            var m2 = u.getClass().getMethod("getPicture");
            Object v2 = m2.invoke(u);
            if (v2 != null && !v2.toString().isBlank()) return v2.toString();
        } catch (Exception ignore) { /* no-op */ }

        return "/image/header/profile-user.png";
    }
}

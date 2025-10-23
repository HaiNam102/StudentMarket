package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Dto.ReviewCreateRequest;
import com.postgresql.StudentMarket.Entities.Review;
import com.postgresql.StudentMarket.Entities.Transaction;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.ReviewRepository;
import com.postgresql.StudentMarket.Repository.TransactionRepository;
import com.postgresql.StudentMarket.Repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final TransactionRepository txRepo;
    private final UserRepository userRepo;

    @Transactional
    public Review create(Integer reviewerId, ReviewCreateRequest req) {
        if (req.getTransactionId() == null)
            throw new IllegalArgumentException("Thiếu transactionId");
        if (req.getRevieweeId() == null)
            throw new IllegalArgumentException("Thiếu revieweeId");
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5)
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");

        if (reviewRepo.existsByTransaction_TransactionIdAndReviewer_UserIdAndReviewee_UserId(
        req.getTransactionId(), reviewerId, req.getRevieweeId())) {
    throw new IllegalStateException("Bạn đã đánh giá người này trong giao dịch này rồi");
}


        var tx = txRepo.findById(req.getTransactionId())
                .orElseThrow(() -> new IllegalArgumentException("Giao dịch không tồn tại"));

        // người đánh giá phải thuộc giao dịch
        if (!tx.getSeller().getUserId().equals(reviewerId) &&
            !tx.getBuyer().getUserId().equals(reviewerId)) {
            throw new IllegalStateException("Bạn không thuộc giao dịch này");
        }

        var reviewer = userRepo.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Người đánh giá không tồn tại"));

        var reviewee = userRepo.findById(req.getRevieweeId())
                .orElseThrow(() -> new IllegalArgumentException("Người bị đánh giá không tồn tại"));

        var r = Review.builder()
                .transaction(tx)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(req.getRating())
                .comment(req.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return reviewRepo.save(r);
    }
}

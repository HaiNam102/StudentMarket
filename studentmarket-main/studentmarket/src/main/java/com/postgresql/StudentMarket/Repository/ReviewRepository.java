package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // --- Check đã đánh giá người này trong giao dịch này chưa ---
    boolean existsByTransaction_TransactionIdAndReviewer_UserIdAndReviewee_UserId(
            Integer transactionId,
            Integer reviewerId,
            Integer revieweeId
    );

    // --- Reviews trong 1 giao dịch mà NGƯỜI NHẬN LÀ TÔI (dùng cho modal "reviews/for-me") ---
    @EntityGraph(attributePaths = {"reviewer", "reviewee", "transaction"})
    List<Review> findByTransaction_TransactionIdAndReviewee_UserId(Integer transactionId, Integer revieweeId);

    // --- Các review TÔI ĐÃ GỬI (dùng cho tab /reviews/mine) ---
    @EntityGraph(attributePaths = {"reviewer", "reviewee", "transaction"})
    Page<Review> findByReviewer_UserIdOrderByCreatedAtDesc(Integer reviewerId, Pageable pageable);

    // (tuỳ chọn) Bản List nếu bạn cần ở chỗ khác
    @EntityGraph(attributePaths = {"reviewer", "reviewee", "transaction"})
    List<Review> findByReviewer_UserIdOrderByCreatedAtDesc(Integer reviewerId);

    // --- (tuỳ chọn) Reviews người khác đánh giá tôi khi tôi là SELLER ---
    @EntityGraph(attributePaths = {"reviewer", "reviewee", "transaction"})
    @Query("""
        select r from Review r
        join r.transaction t
        where r.reviewee.userId = :me
          and t.seller.userId   = :me
        order by r.createdAt desc
    """)
    List<Review> findReceivedWhenImSeller(@Param("me") Integer me);

    // --- (tuỳ chọn) Reviews người khác đánh giá tôi khi tôi là BUYER ---
    @EntityGraph(attributePaths = {"reviewer", "reviewee", "transaction"})
    @Query("""
        select r from Review r
        join r.transaction t
        where r.reviewee.userId = :me
          and t.buyer.userId    = :me
        order by r.createdAt desc
    """)
    List<Review> findReceivedWhenImBuyer(@Param("me") Integer me);

    // --- Lấy các review tôi đã viết trong một danh sách giao dịch (optional helper) ---
    @EntityGraph(attributePaths = {"reviewer", "reviewee", "transaction"})
    List<Review> findByReviewer_UserIdAndTransaction_TransactionIdIn(Integer reviewerId, List<Integer> txIds);

    // --- Trả về danh sách transactionId mà TÔI đã review (để FE ẩn nút Đánh giá) ---
    @Query("""
        select distinct r.transaction.transactionId
        from Review r
        where r.reviewer.userId = :userId
          and r.transaction.transactionId in :txIds
    """)
    List<Integer> findReviewedTxIdsByMe(@Param("userId") Integer userId,
                                        @Param("txIds") List<Integer> txIds);

                                        @Query("""
   select r from Review r
   where r.reviewer.userId = :me
   order by r.createdAt desc
""")
List<Review> findWrittenByMe(@Param("me") Integer me);
}

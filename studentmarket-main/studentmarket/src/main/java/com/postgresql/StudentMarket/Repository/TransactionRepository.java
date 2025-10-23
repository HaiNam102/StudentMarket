package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.Transaction;
import com.postgresql.StudentMarket.Entities.Transaction.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

        @EntityGraph(attributePaths = { "details", "details.product", "seller" })
        Page<Transaction> findByBuyer_UserIdAndStatusOrderByCreatedAtDesc(Integer buyerId, Status status,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "details", "details.product", "seller" })
        List<Transaction> findByBuyer_UserIdOrderByCreatedAtDesc(Integer buyerId);

        long countByBuyer_UserIdAndStatus(Integer buyerId, Status status);

        @EntityGraph(attributePaths = { "details", "details.product", "seller" })
        Page<Transaction> findByBuyer_UserIdInAndStatusOrderByCreatedAtDesc(List<Integer> buyerIds, Status status,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "details", "details.product", "seller" })
        List<Transaction> findByBuyer_UserIdInOrderByCreatedAtDesc(List<Integer> buyerIds);

        @EntityGraph(attributePaths = { "details", "details.product", "seller" })
        Page<Transaction> findByStatusOrderByCreatedAtDesc(Status status, Pageable pageable);

        long countByStatus(Status status);

        long countByBuyer_UserIdInAndStatus(List<Integer> buyerIds, Status status);

        // ✅ Thêm mới để lấy “giao dịch của tôi” (buyer hoặc seller)
        @Query("select t from Transaction t " +
                        "where (t.buyer.userId = :uid or t.seller.userId = :uid) " +
                        "and t.status = :status " +
                        "order by t.createdAt desc")
        Page<Transaction> findMineByStatus(@Param("uid") Integer uid,
                        @Param("status") Transaction.Status status,
                        Pageable pageable);

        @Query("select count(t) from Transaction t " +
                        "where (t.buyer.userId = :uid or t.seller.userId = :uid) and t.status = :status")
        long countMineByStatus(@Param("uid") Integer uid,
                        @Param("status") Transaction.Status status);

        @EntityGraph(attributePaths = { "details", "details.product", "seller" })
        Page<Transaction> findBySeller_UserIdAndStatusOrderByCreatedAtDesc(Integer sellerId, Status status,
                        Pageable pageable);

        long countBySeller_UserIdAndStatus(Integer sellerId, Status status);
}

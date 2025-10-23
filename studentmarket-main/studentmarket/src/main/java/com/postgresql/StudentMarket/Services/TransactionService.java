package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Entities.Transaction;
import com.postgresql.StudentMarket.Repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repo;

    public Page<Transaction> pageAllByStatus(Transaction.Status status, int page, int size) {
        return repo.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
    }

    public Map<String, Long> countAllByStatus() {
        Map<String, Long> m = new HashMap<>();
        for (Transaction.Status st : Transaction.Status.values()) {
            long c = repo.countByStatus(st);
            m.put(st.name(), c);
        }
        return m;
    }

    public Page<Transaction> pageByBuyerAndStatus(Integer buyerId, Transaction.Status status, int page, int size) {
        return repo.findByBuyer_UserIdAndStatusOrderByCreatedAtDesc(buyerId, status, PageRequest.of(page, size));
    }

    public Page<Transaction> pageByBuyersAndStatus(List<Integer> buyerIds, Transaction.Status status, int page, int size) {
        return repo.findByBuyer_UserIdInAndStatusOrderByCreatedAtDesc(buyerIds, status, PageRequest.of(page, size));
    }

    public List<Transaction> historyByBuyer(Integer buyerId) {
        return repo.findByBuyer_UserIdOrderByCreatedAtDesc(buyerId);
    }

    public List<Transaction> historyByBuyers(List<Integer> buyerIds) {
        return repo.findByBuyer_UserIdInOrderByCreatedAtDesc(buyerIds);
    }

    public Map<String, Long> countByStatus(List<Transaction> list) {
        return list.stream().collect(Collectors.groupingBy(
                t -> t.getStatus().name(),
                Collectors.counting()
        ));
    }

    public Map<String, Long> countByBuyers(List<Integer> buyerIds) {
        Map<String, Long> result = new HashMap<>();
        for (Transaction.Status st : Transaction.Status.values()) {
            long c = repo.countByBuyer_UserIdInAndStatus(buyerIds, st);
            result.put(st.name(), c);
        }
        return result;
    }

    // ✅ Đếm giao dịch của chính user (buyer hoặc seller)
    public Map<String, Long> countByUser(Integer userId) {
        Map<String, Long> result = new HashMap<>();
        for (Transaction.Status st : Transaction.Status.values()) {
            long c = repo.countMineByStatus(userId, st);
            result.put(st.name(), c);
        }
        return result;
    }

    @Transactional
    public void updateStatus(Integer id, Transaction.Status newStatus) {
        var tx = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        tx.setStatus(newStatus);
        tx.setUpdatedAt(LocalDateTime.now());
        switch (newStatus) {
            case COMPLETED -> {
                tx.setCompletedAt(LocalDateTime.now());
                tx.setCancelledAt(null);
            }
            case CANCELLED -> {
                tx.setCancelledAt(LocalDateTime.now());
                tx.setCompletedAt(null);
            }
            default -> { }
        }
    }

    // TransactionService.java
public Page<Transaction> pageBySellerAndStatus(Integer sellerId, Transaction.Status status, int page, int size) {
    return repo.findBySeller_UserIdAndStatusOrderByCreatedAtDesc(sellerId, status, PageRequest.of(page, size));
}

public Map<String, Long> countByBuyer(Integer buyerId) {
    Map<String, Long> result = new HashMap<>();
    for (Transaction.Status st : Transaction.Status.values()) {
        long c = repo.countByBuyer_UserIdAndStatus(buyerId, st);
        result.put(st.name(), c);
    }
    return result;
}

public Map<String, Long> countBySeller(Integer sellerId) {
    Map<String, Long> result = new HashMap<>();
    for (Transaction.Status st : Transaction.Status.values()) {
        long c = repo.countBySeller_UserIdAndStatus(sellerId, st);
        result.put(st.name(), c);
    }
    return result;
}

}

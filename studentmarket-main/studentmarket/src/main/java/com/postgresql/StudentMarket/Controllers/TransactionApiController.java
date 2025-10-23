package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ReviewItemDTO;
import com.postgresql.StudentMarket.Dto.TransactionCardDTO;
import com.postgresql.StudentMarket.Entities.Review;
import com.postgresql.StudentMarket.Entities.Transaction;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.ReviewRepository;
import com.postgresql.StudentMarket.Repository.TransactionRepository;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.TransactionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionApiController {
    private final TransactionService service;
    private final TransactionRepository txRepo;
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;

    private Integer meId(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null || principal.getAttribute("email") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }
        return userRepo.findByEmail(principal.getAttribute("email"))
                .map(User::getUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy user"));
    }

    // ======== API hiển thị danh sách =========
    @GetMapping
    public Page<TransactionCardDTO> list(
            @RequestParam Transaction.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<Integer> buyerIds) {

        var pageTx = (buyerIds != null && !buyerIds.isEmpty())
                ? service.pageByBuyersAndStatus(buyerIds, status, page, size)
                : service.pageAllByStatus(status, page, size);

        return pageTx.map(TransactionCardDTO::of);
    }

    @GetMapping("/counts")
    public Map<String, Long> counts(@RequestParam(required = false) List<Integer> buyerIds) {
        if (buyerIds != null && !buyerIds.isEmpty())
            return service.countByBuyers(buyerIds);
        return service.countAllByStatus();
    }

    // ======== API chỉ lấy giao dịch của user đang đăng nhập =========
    // TransactionApiController.java
    @GetMapping("/mine")
    public Page<TransactionCardDTO> mine(
            @RequestParam Transaction.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "BUYER") String role, // ⬅️ thêm
            @AuthenticationPrincipal OAuth2User principal) {

        Integer me = meId(principal);

        Page<Transaction> pageTx;
        if ("SELLER".equalsIgnoreCase(role)) {
            pageTx = service.pageBySellerAndStatus(me, status, page, size);
        } else { // mặc định BUYER
            pageTx = service.pageByBuyerAndStatus(me, status, page, size);
        }
        return pageTx.map(TransactionCardDTO::of);
    }

    // TransactionApiController.java
    @GetMapping("/counts/mine")
    public Map<String, Long> myCounts(@RequestParam(defaultValue = "BUYER") String role,
            @AuthenticationPrincipal OAuth2User principal) {
        Integer me = meId(principal);
        if ("SELLER".equalsIgnoreCase(role)) {
            return service.countBySeller(me);
        }
        return service.countByBuyer(me); // mặc định BUYER
    }

    // ======== API Hủy giao dịch =========
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Integer id) {
        var tx = txRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch"));
        service.updateStatus(tx.getTransactionId(), Transaction.Status.CANCELLED);
        return ResponseEntity.noContent().build();
    }

    // ======== API Đánh giá =========
    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rate(@PathVariable Integer id,
            @RequestBody ReviewCreateReq body,
            @AuthenticationPrincipal OAuth2User principal) {
        Integer me = meId(principal);

        var tx = txRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch"));

        if (tx.getStatus() != Transaction.Status.COMPLETED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Chỉ đánh giá khi giao dịch đã hoàn thành");
        }

        var seller = tx.getSeller();
        var buyer = tx.getBuyer();
        if (seller == null || buyer == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Thiếu thông tin buyer/seller");
        }

        boolean iAmSeller = Objects.equals(seller.getUserId(), me);
        boolean iAmBuyer = Objects.equals(buyer.getUserId(), me);
        if (!iAmSeller && !iAmBuyer) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không thuộc giao dịch này");
        }

        Integer expectedRevieweeId = iAmSeller ? buyer.getUserId() : seller.getUserId();
        Integer revieweeId = (body.getRatedUserId() != null) ? body.getRatedUserId() : expectedRevieweeId;

        if (!Objects.equals(revieweeId, expectedRevieweeId)) {
            return ResponseEntity.badRequest().body("revieweeId không hợp lệ");
        }
        if (Objects.equals(me, revieweeId)) {
            return ResponseEntity.badRequest().body("Không thể tự đánh giá chính mình");
        }

        boolean exists = reviewRepo.existsByTransaction_TransactionIdAndReviewer_UserIdAndReviewee_UserId(
                tx.getTransactionId(), me, revieweeId);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Bạn đã đánh giá người này trong giao dịch này rồi");
        }

        var reviewer = userRepo.findById(me).orElseThrow();
        var reviewee = userRepo.findById(revieweeId).orElseThrow();

        Review r = new Review();
        r.setTransaction(tx);
        r.setReviewer(reviewer);
        r.setReviewee(reviewee);
        r.setRating(Optional.ofNullable(body.getStars()).orElse(5));
        r.setComment(Optional.ofNullable(body.getContent()).orElse("").trim());
        r.setCreatedAt(LocalDateTime.now());
        reviewRepo.save(r);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reviews/{id}")
                .buildAndExpand(r.getReviewId())
                .toUri();

        return ResponseEntity.created(location).body(Map.of(
                "ok", true,
                "transactionId", id,
                "reviewId", r.getReviewId()));
    }

    @Data
    public static class ReviewCreateReq {
        private Integer ratedUserId;
        private Integer stars;
        private String content;
    }

    @Data
    public static class NoteUpdateReq {
        private String note;
    }

    @PatchMapping("/{id}/note")
    public ResponseEntity<?> updateNote(@PathVariable Integer id,
            @RequestBody NoteUpdateReq body,
            @AuthenticationPrincipal OAuth2User principal) {
        Integer me = meId(principal);

        var tx = txRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch"));

        // Chỉ cho sửa khi ở trạng thái ĐANG YÊU CẦU
        if (tx.getStatus() != Transaction.Status.REQUESTING) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Chỉ được sửa ghi chú khi giao dịch đang yêu cầu");
        }

        // Quyền: người mua (buyer) là người đã gửi yêu cầu (hoặc cho phép seller tùy
        // bạn)
        var buyer = tx.getBuyer();
        if (buyer == null || !Objects.equals(buyer.getUserId(), me)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không thể sửa ghi chú giao dịch này");
        }

        String note = Optional.ofNullable(body.getNote()).orElse("").trim();
        // (tuỳ ý) giới hạn độ dài
        if (note.length() > 1000) {
            return ResponseEntity.badRequest().body("Ghi chú quá dài (tối đa 1000 ký tự)");
        }

        tx.setNote(note);
        tx.setUpdatedAt(LocalDateTime.now());
        txRepo.save(tx);

        // trả về DTO để FE cập nhật ngay
        return ResponseEntity.ok(TransactionCardDTO.of(tx));
    }

    @GetMapping("/{id}/reviews/for-me")
    public List<ReviewItemDTO> reviewsForMe(@PathVariable Integer id,
            @AuthenticationPrincipal OAuth2User principal) {
        Integer me = meId(principal);
        var list = reviewRepo.findByTransaction_TransactionIdAndReviewee_UserId(id, me);
        return list.stream().map(ReviewItemDTO::of).toList();
    }

    @GetMapping("/reviews/mine")
    public Page<ReviewItemDTO> myReviews(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal OAuth2User principal) {
        Integer me = meId(principal);
        var p = reviewRepo.findByReviewer_UserIdOrderByCreatedAtDesc(me,
                org.springframework.data.domain.PageRequest.of(page, size));
        return p.map(ReviewItemDTO::of);
    }
}

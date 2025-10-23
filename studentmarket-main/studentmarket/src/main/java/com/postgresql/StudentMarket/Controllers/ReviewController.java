package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ReviewCreateRequest;
import com.postgresql.StudentMarket.Entities.Review;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepo;

    private Integer meId(@AuthenticationPrincipal OAuth2User principal) {
        return userRepo.findByEmail(principal.getAttribute("email"))
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal OAuth2User principal,
                                    @RequestBody ReviewCreateRequest req) {
        Integer reviewerId = meId(principal);
        Review saved = reviewService.create(reviewerId, req);
        return ResponseEntity.ok(Map.of(
                "reviewId", saved.getReviewId(),
                "createdAt", saved.getCreatedAt().toString()
        ));
    }
}

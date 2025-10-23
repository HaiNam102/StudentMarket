package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.ReviewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewQueryController {

    private final ReviewQueryService reviewQueryService;
    private final UserRepository userRepo;

    private Integer meId(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null || principal.getAttribute("email") == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        return userRepo.findByEmail(principal.getAttribute("email"))
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    @GetMapping("/categorized")
    public ResponseEntity<?> getCategorized(@AuthenticationPrincipal OAuth2User principal) {
        Integer me = meId(principal);
        return ResponseEntity.ok(reviewQueryService.getCategorized(me));
    }

    // GET /api/reviews/rated-by-me?txIds=1,2,3  (comma-separated OK)
    @GetMapping("/rated-by-me")
    public ResponseEntity<?> ratedByMe(@AuthenticationPrincipal OAuth2User principal,
                                       @RequestParam(value = "txIds", required = false) List<Integer> txIds) {
        Integer me = meId(principal);
        if (txIds == null || txIds.isEmpty()) {
            return ResponseEntity.ok(Map.of("ratedTxIds", Collections.emptyList()));
        }
        var reviewed = reviewQueryService.findReviewedTxIdsByMe(me, txIds);
        return ResponseEntity.ok(Map.of("ratedTxIds", reviewed));
    }
}

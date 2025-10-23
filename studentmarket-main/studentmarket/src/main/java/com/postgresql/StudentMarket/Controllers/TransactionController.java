package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.Transaction;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService historyService;
    private final UserRepository userRepository;

    @GetMapping("/lichsugiaodich")
    public String page(@AuthenticationPrincipal OAuth2User principal,
                       @RequestParam(required = false) Integer buyerId,
                       @RequestParam(required = false) java.util.List<Integer> buyerIds,
                       Model model) {

        if (buyerId != null) {
            model.addAttribute("buyerIds", java.util.List.of(buyerId));
        } else if (buyerIds != null && !buyerIds.isEmpty()) {
            model.addAttribute("buyerIds", buyerIds);
        }
        return "lichsugiaodich";
    }

    @PostMapping("/studentmarket/transactions/{id}/status")
    public String updateStatus(@PathVariable Integer id, @RequestParam("status") Transaction.Status status) {
        historyService.updateStatus(id, status);
        return "redirect:/lichsugiaodich";
    }
}

package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    @GetMapping("/gopy")
    public String gopyPage() {
        return "gopy";
    }

    @PostMapping("/gopy")
    public String submitFeedback(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam("issueType") String issueType,
            @RequestParam("feedbackText") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes ra
    ) {
        try {
            if (oauth2User == null) {
                ra.addFlashAttribute("error", "Bạn cần đăng nhập để gửi góp ý!");
                return "redirect:/gopy";
            }

            String email = (String) oauth2User.getAttributes().get("email");
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                ra.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/gopy";
            }

            feedbackService.saveFeedback(user, issueType, content, file);
            ra.addFlashAttribute("success", "Góp ý của bạn đã được gửi!");
            return "redirect:/gopy";

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Gửi góp ý thất bại: " + e.getMessage());
            return "redirect:/gopy";
        }
    }
}

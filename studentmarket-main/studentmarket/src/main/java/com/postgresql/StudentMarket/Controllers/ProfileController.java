package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.Product;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.ProductRepository;
import com.postgresql.StudentMarket.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ==== Helpers ====
    private boolean isOnline(LocalDateTime lastSeen) {
        return lastSeen != null && Duration.between(lastSeen, LocalDateTime.now()).toMinutes() < 1;
    }

    private String humanizeLastSeen(LocalDateTime lastSeen) {
        if (lastSeen == null) return "Chưa từng hoạt động";
        Duration d = Duration.between(lastSeen, LocalDateTime.now());
        long sec = d.getSeconds();
        if (sec < 60) return "Vừa xong";
        long min = sec / 60;
        if (min < 60) return "Hoạt động " + min + " phút trước";
        long hrs = min / 60;
        if (hrs < 24) return "Hoạt động " + hrs + " giờ trước";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "Hoạt động " + lastSeen.format(fmt);
    }

    private User resolveCurrentUser(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("user_id");
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (email != null) {
                User me = userRepository.findByEmail(email).orElse(null);
                if (me != null) {
                    session.setAttribute("user_id", me.getUserId());
                    return me;
                }
            }
        }
        return null;
    }

    /** Kiểm tra user là ADMIN qua bảng user_role ↔ role. */
    private boolean isAdmin(Integer userId) {
        return userId != null && userRepository.countAdminRoles(userId) > 0;
    }

    // ============ Xem profile theo {id} (bao gồm cả chính mình) ============
    @GetMapping("/StudentMarket/xemprofile/{id}")
    public String viewUserProfile(
            @PathVariable("id") Integer userId,
            @AuthenticationPrincipal OAuth2User principal,
            HttpSession session,
            Model model
    ) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy người dùng có ID " + userId));

        // Ẩn profile của mọi tài khoản ADMIN (dù ID nào)
        if (isAdmin(target.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }

        // Ai đang đăng nhập?
        User current = resolveCurrentUser(principal, session);
        boolean isSelf = current != null && current.getUserId().equals(target.getUserId());

        // Bind dữ liệu chung
        model.addAttribute("target", target);
        LocalDateTime lastSeen = target.getLastSeenAt();
        model.addAttribute("isOnline", isOnline(lastSeen));
        model.addAttribute("lastSeenHuman", humanizeLastSeen(lastSeen));

        List<Product> posted = productRepository.findByUserIdOrderByCreatedAtDesc(target.getUserId());
        model.addAttribute("posted", posted);
        model.addAttribute("postedCount", posted != null ? posted.size() : 0);

        return isSelf ? "xemprofilecanhan" : "xemprofile";
    }

    // ===== API cập nhật last-seen (cũng ẩn với ADMIN) =====
    @GetMapping("/api/users/{id}/last-seen")
    @ResponseBody
    public java.util.Map<String, String> getLastSeen(@PathVariable Integer id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        if (isAdmin(u.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng");
        }
        String iso = null;
        if (u.getLastSeenAt() != null) {
            iso = u.getLastSeenAt().toString().replace(' ', 'T');
        }
        return java.util.Collections.singletonMap("lastSeenIso", iso);
    }
}

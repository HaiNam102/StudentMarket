package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.session.Session;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Trang thông tin tài khoản + hiển thị thiết bị đăng nhập (sessions)
 */
@Controller
public class HomePageController {

    private final UserRepository userRepository;
    private final JdbcIndexedSessionRepository sessionRepo;

    public HomePageController(UserRepository userRepository,
                              JdbcIndexedSessionRepository sessionRepo) {
        this.userRepository = userRepository;
        this.sessionRepo = sessionRepo;
    }

    /**
     * ALWAYS cung cấp "sessions" cho view ở cả GET/POST.
     */
    @ModelAttribute("sessions")
    public List<Map<String, Object>> populateSessions(Authentication authentication,
                                                      HttpServletRequest request) {
        String principalName = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : null;
        if (principalName == null) return Collections.emptyList();

        Map<String, ? extends Session> map = sessionRepo.findByPrincipalName(principalName);
        String currentId = (request.getSession(false) != null) ? request.getSession(false).getId() : null;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
                .withZone(ZoneId.systemDefault());

        List<Map<String, Object>> sessions = new ArrayList<>();
        for (Session s : map.values()) {
            Map<String, Object> row = new HashMap<>();
            row.put("sessionId", s.getId());
            row.put("creation", fmt.format(s.getCreationTime()));
            row.put("lastAccess", fmt.format(s.getLastAccessedTime()));
            row.put("isCurrent", Objects.equals(currentId, s.getId()));
            row.put("ua", Optional.ofNullable((String) s.getAttribute("USER_AGENT")).orElse("—"));
            row.put("deviceId", Optional.ofNullable((String) s.getAttribute("DEVICE_ID")).orElse("—"));
            row.put("ip", Optional.ofNullable((String) s.getAttribute("IP_ADDRESS")).orElse("—"));
            // phục vụ sort
            row.put("_lastAccessEpoch", s.getLastAccessedTime().toEpochMilli());
            sessions.add(row);
        }

        // Sắp xếp lần truy cập gần nhất trước (dựa trên epoch, chính xác hơn sort theo chuỗi)
        sessions.sort((a, b) -> Long.compare(
                (long) b.getOrDefault("_lastAccessEpoch", 0L),
                (long) a.getOrDefault("_lastAccessEpoch", 0L)
        ));
        sessions.forEach(m -> m.remove("_lastAccessEpoch")); // dọn key tạm

        return sessions;
    }

    // Trang thông tin tài khoản cho USER sau khi đăng nhập
    @GetMapping("/thongtintaikhoan")
    public String thongTinTaiKhoan(@AuthenticationPrincipal OAuth2User principal,
                                   HttpSession session,
                                   Model model) {
        if (principal == null) {
            return "redirect:/StudentMarket";
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            // Đồng bộ header theo session
            session.setAttribute("full_name", user.getFullName());
            session.setAttribute("picture", user.getPicture());
            session.setAttribute("email", user.getEmail());
        }

        return "thongtintaikhoan";
    }

    // Lưu thay đổi hồ sơ (họ tên, giới tính, số điện thoại, địa chỉ, ngày sinh)
    @PostMapping("/thongtintaikhoan")
    public String luuThongTin(@AuthenticationPrincipal OAuth2User principal,
                              Model model,
                              HttpSession session,
                              RedirectAttributes ra,
                              @RequestParam("fullName") String fullName,
                              @RequestParam(value = "gender", required = false) String gender,
                              @RequestParam(value = "phone", required = false) String phone,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "dateOfBirth", required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth) {
        if (principal == null) return "redirect:/StudentMarket";

        String currentEmail = principal.getAttribute("email");
        User user = userRepository.findByEmail(currentEmail).orElse(null);
        if (user == null) return "redirect:/StudentMarket";

        // Validate số điện thoại
        String digits = (phone == null) ? "" : phone.replaceAll("\\D", "");
        if (!digits.matches("^0\\d{9}$")) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Số điện thoại không hợp lệ.");
            return "thongtintaikhoan"; // @ModelAttribute sẽ tự nạp sessions
        }

        // Validate ngày sinh
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Ngày sinh không được lớn hơn ngày hiện tại.");
            return "thongtintaikhoan"; // @ModelAttribute sẽ tự nạp sessions
        }

        // Cập nhật
        user.setFullName(fullName);
        if (gender != null) user.setGender(normalizeGender(gender));
        user.setPhone(digits);
        user.setAddress((address == null || address.isBlank()) ? null : address.trim());
        user.setDateOfBirth(dateOfBirth);
        userRepository.save(user);

        // Cập nhật session cho header
        session.setAttribute("full_name", user.getFullName());
        session.setAttribute("picture", user.getPicture());
        session.setAttribute("email", user.getEmail());

        // PRG để tránh F5 resubmit & luôn có sessions từ GET
        ra.addFlashAttribute("success", "Đã lưu thay đổi!");
        return "redirect:/thongtintaikhoan";
    }

    // Map "Nam/Nữ/Khác" -> MALE/FEMALE/OTHER
    private String normalizeGender(String g) {
        String s = (g == null) ? "" : g.trim().toUpperCase();
        if (s.equals("NAM") || s.equals("MALE")) return "MALE";
        if (s.equals("NỮ") || s.equals("NU") || s.equals("FEMALE")) return "FEMALE";
        return "OTHER";
    }

    @GetMapping("/dieukhoansudung")
    public String dieukhoansudung() {
        return "dieukhoansudung";
    }
}

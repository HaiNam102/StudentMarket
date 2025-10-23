package com.postgresql.StudentMarket.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes; // << thêm

import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.Session;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.HttpServletRequest;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class DeviceSessionController {

    private final JdbcIndexedSessionRepository sessionRepo;

    public DeviceSessionController(JdbcIndexedSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    @GetMapping("/devices")
    public String listDevices(@AuthenticationPrincipal OAuth2User principal,
            Authentication authentication,
            HttpServletRequest request,
            Model model) {
        String principalName = resolvePrincipalName(principal, authentication);
        if (principalName == null)
            return "redirect:/StudentMarket";

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
            row.put("ip",       Optional.ofNullable((String) s.getAttribute("IP_ADDRESS")).orElse("—"));
            sessions.add(row);
        }

        model.addAttribute("sessions", sessions);
        return "thongtintaikhoan";
    }

    @PostMapping("/devices/logout")
    public String logoutDevices(@AuthenticationPrincipal OAuth2User principal,
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam(name = "sessionIds", required = false) List<String> sessionIds,
            RedirectAttributes ra) { // << dùng ở đây
        String principalName = resolvePrincipalName(principal, authentication);
        if (principalName == null)
            return "redirect:/homeGUEST";

        if (sessionIds == null || sessionIds.isEmpty()) {
            ra.addFlashAttribute("error", "Bạn chưa chọn thiết bị nào.");
            return "redirect:/thongtintaikhoan";
        }

        String currentId = (request.getSession(false) != null) ? request.getSession(false).getId() : null;
        int count = 0;
        for (String id : sessionIds) {
            if (Objects.equals(id, currentId))
                continue;
            try {
                sessionRepo.deleteById(id);
                count++;
            } catch (Exception ignored) {
            }
        }
        ra.addFlashAttribute("success", "Đã đăng xuất " + count + " thiết bị.");
        return "redirect:/thongtintaikhoan";
    }

    @PostMapping("/devices/logout-all-others")
    public String logoutAllOthers(@AuthenticationPrincipal OAuth2User principal,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes ra) { // << dùng ở đây
        String principalName = resolvePrincipalName(principal, authentication);
        if (principalName == null)
            return "redirect:/StudentMarket";

        Map<String, ? extends Session> map = sessionRepo.findByPrincipalName(principalName);
        String currentId = (request.getSession(false) != null) ? request.getSession(false).getId() : null;
        int count = 0;
        for (Session s : map.values()) {
            if (Objects.equals(s.getId(), currentId))
                continue;
            try {
                sessionRepo.deleteById(s.getId());
                count++;
            } catch (Exception ignored) {
            }
        }
        ra.addFlashAttribute("success", "Đã đăng xuất " + count + " thiết bị khác.");
        return "redirect:/thongtintaikhoan";
    }

    private String resolvePrincipalName(OAuth2User principal, Authentication authentication) {
        // Spring Session JDBC dùng authentication.getName() để ghi vào cột
        // PRINCIPAL_NAME
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName(); // ví dụ: với OAuth2 là "sub"
            if (name != null && !name.isBlank())
                return name;
        }
        // Fallback: nếu vì lý do nào đó bạn tự cấu hình PRINCIPAL_NAME là email
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (email != null && !email.isBlank())
                return email;
        }
        return null;
    }
}

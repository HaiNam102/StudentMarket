package com.postgresql.StudentMarket.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class UserAgentCaptureFilter implements Filter {

    private static final String COOKIE_NAME = "DEVICE_ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest http && response instanceof HttpServletResponse resp) {
            HttpSession session = http.getSession(true);

            // --- Lưu thông tin User-Agent ---
            String raw = http.getHeader("User-Agent");
            String label = buildLabel(raw); // OS (Browser)
            session.setAttribute("USER_AGENT", label);
            session.setAttribute("USER_AGENT_RAW", raw);

            // --- Lưu địa chỉ IP ---
            String ip = http.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = http.getRemoteAddr();
            session.setAttribute("IP_ADDRESS", ip);

            // --- Gắn cookie DEVICE_ID duy nhất ---
            String deviceId = getOrCreateDeviceId(http, resp);
            session.setAttribute("DEVICE_ID", deviceId);
        }

        chain.doFilter(request, response);
    }

    /** Lấy hoặc tạo cookie DEVICE_ID cho trình duyệt */
    private String getOrCreateDeviceId(HttpServletRequest req, HttpServletResponse resp) {
        String deviceId = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (COOKIE_NAME.equals(c.getName())) {
                    deviceId = c.getValue();
                    break;
                }
            }
        }

        if (deviceId == null || deviceId.isBlank()) {
            deviceId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(COOKIE_NAME, deviceId);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(60 * 60 * 24 * 365 * 5); // 5 năm
            // cookie.setSecure(true); // bật nếu dùng HTTPS
            resp.addCookie(cookie);
        }
        return deviceId;
    }

    private String buildLabel(String ua) {
        String os = detectOS(ua);
        String br = detectBrowser(ua);
        if ("Unknown".equals(os) && "Unknown".equals(br)) return ua != null ? ua : "—";
        if (!"Unknown".equals(os) && !"Unknown".equals(br)) return os + " (" + br + ")";
        return !"Unknown".equals(os) ? os : br;
    }

    private String detectOS(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone")) return "iPhone";
        if (ua.contains("iPad")) return "iPad";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }

    private String detectBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("OPR/") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Chrome/")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/") && !ua.contains("Chrome/")) return "Safari";
        return "Unknown";
    }
}

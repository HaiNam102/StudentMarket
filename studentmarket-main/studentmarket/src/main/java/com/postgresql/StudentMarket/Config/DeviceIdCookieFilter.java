package com.postgresql.StudentMarket.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class DeviceIdCookieFilter implements Filter {

    public static final String COOKIE_NAME = "DEVICE_ID";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (req instanceof HttpServletRequest http && res instanceof HttpServletResponse resp) {
            String deviceId = null;
            if (http.getCookies() != null) {
                for (Cookie c : http.getCookies()) {
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
                // cookie.setSecure(true); // bật nếu chạy HTTPS
                resp.addCookie(cookie);
            }
            // Lưu vào session cho tiện tra cứu khi liệt kê
            HttpSession session = http.getSession(true);
            session.setAttribute("DEVICE_ID", deviceId);
        }
        chain.doFilter(req, res);
    }
}

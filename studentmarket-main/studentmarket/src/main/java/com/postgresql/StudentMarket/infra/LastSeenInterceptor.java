package com.postgresql.StudentMarket.infra;

import com.postgresql.StudentMarket.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LastSeenInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Integer uid = (Integer) request.getSession().getAttribute("user_id");
        if (uid != null) {
            // Giảm ghi DB: tối đa 60s mới cập nhật một lần
            Long next = (Long) request.getSession().getAttribute("last_seen_next_update");
            long now = System.currentTimeMillis();
            if (next == null || now >= next) {
                userRepository.findById(uid).ifPresent(u -> {
                    u.setLastSeenAt(LocalDateTime.now());
                    userRepository.save(u);
                });
                request.getSession().setAttribute("last_seen_next_update", now + 60_000);
            }
        }
        return true;
    }
}

// LoginSecurity.java
package com.postgresql.StudentMarket.Security;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class LoginSecurity {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;

    /**
     * Success handler:
     * - joined_at: set ở lần đăng nhập đầu tiên
     * - last_seen_at: luôn cập nhật khi đăng nhập
     * - set session cho header/hosoprofile: user_id, full_name, joined_at(_str), last_seen_at(_iso)
     * - chuyển về /StudentMarket
     */
    @Bean
    public AuthenticationSuccessHandler firstLoginSuccessHandler() {
        return (request, response, authentication) -> {
            String email = null;

            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oAuth2User) {
                email = oAuth2User.getAttribute("email");
            } else if (principal instanceof UserDetails ud) {
                email = ud.getUsername(); // với form-login, thường username = email
            }

            if (email != null) {
                User u = userRepository.findByEmail(email).orElse(null);
                if (u != null) {
                    boolean changed = false;

                    // 1) joined_at: lần đầu đăng nhập
                    if (u.getJoinedAt() == null) {
                        u.setJoinedAt(LocalDate.now());
                        changed = true;
                    }

                    // 2) last_seen_at: luôn cập nhật khi login
                    LocalDateTime now = LocalDateTime.now();
                    u.setLastSeenAt(now);
                    changed = true;

                    if (changed) userRepository.save(u);

                    // 3) Nhét session để template dùng ngay
                    request.getSession().setAttribute("user_id", u.getUserId());
                    request.getSession().setAttribute("full_name", u.getFullName());
                    request.getSession().setAttribute("joined_at", u.getJoinedAt());

                    // tiện: format sẵn để không cần #temporals
                    DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    request.getSession().setAttribute("joined_at_str",
                            u.getJoinedAt() != null ? u.getJoinedAt().format(dmy) : null);

                    // last_seen (để JS hiển thị “X phút trước” nếu cần)
                    request.getSession().setAttribute("last_seen_at", u.getLastSeenAt());
                    // ISO đơn giản cho JS parse
                    DateTimeFormatter iso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    request.getSession().setAttribute("last_seen_at_iso",
                            u.getLastSeenAt() != null ? u.getLastSeenAt().format(iso) : null);
                }
            }

            response.sendRedirect("/StudentMarket");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
            .ignoringRequestMatchers(
                "/api/transactions/**" // Nếu API này dùng fetch có CSRF token thủ công thì có thể để mặc định
            )
        )
            .authorizeHttpRequests(auth -> auth
                // Công khai
                .requestMatchers(
                    "/", "/StudentMarket",
                    "/error", "/error/**",
                    "/css/**", "/js/**", "/image/**", "/assets/**",
                    "/webjars/**", "/oauth2/**"
                ).permitAll()
                .requestMatchers("/api/transactions/**").authenticated()

                // Khu cần đăng nhập
                .requestMatchers(
                    "/thongtintaikhoan", "/tongquan",
                    "/dieukhoansudung", "/gopy",
                    "/dangbai",
                    "/devices/**"
                ).authenticated()

                // Những route khác tạm cho phép
                .anyRequest().permitAll()
            )

            // Đăng nhập OAuth2 (Google)
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                .successHandler(firstLoginSuccessHandler()) // dùng handler ở trên
                .failureHandler((request, response, exception) -> {
                    request.getSession().setAttribute("flash_error", exception.getMessage());
                    response.sendRedirect("/StudentMarket");
                })
            )

            // (Tuỳ chọn) Form login truyền thống
            // .formLogin(form -> form
            //     .loginPage("/login").permitAll()
            //     .successHandler(firstLoginSuccessHandler())
            // )

            // Đăng xuất
            .logout(logout -> logout
                .logoutUrl("/logout") // POST
                .logoutSuccessHandler((request, response, authentication) -> {
                    request.getSession().setAttribute("flash_success", "Bạn đã đăng xuất thành công!");
                    response.sendRedirect("/StudentMarket");
                })
                .clearAuthentication(true)
                .invalidateHttpSession(true)
            );

        return http.build();
    }
}

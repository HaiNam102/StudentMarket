package com.postgresql.StudentMarket.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = "/studentmarket/homepage"; 

        // Kiểm tra quyền
        var authorities = authentication.getAuthorities();
        for (var authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                redirectUrl = "/studentmarket/admin/homepageAdmin";
                break;
            }
        }

        // Redirect
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

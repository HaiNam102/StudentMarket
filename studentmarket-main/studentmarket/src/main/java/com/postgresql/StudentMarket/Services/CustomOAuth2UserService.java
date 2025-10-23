package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String email      = getString(attrs, "email");
        String sub        = getString(attrs, "sub");            // google_id
        String googleName = getString(attrs, "name");
        String givenName  = getString(attrs, "given_name");
        String familyName = getString(attrs, "family_name");
        String picture    = getString(attrs, "picture");
        Boolean verified  = getBoolean(attrs, "email_verified");

        // Ràng buộc domain email
        if (email == null || !email.endsWith("@dtu.edu.vn")) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token",
                    "Vui lòng sử dụng email @dtu.edu.vn",
                    null)
            );
        }

        // Phải là email đã được Google xác minh
        if (!Boolean.TRUE.equals(verified)) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token",
                    "Email Google của bạn chưa được xác minh (email_verified = false).",
                    null)
            );
        }

        // Tìm theo email trước, fallback theo google_id
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null && sub != null) {
            user = userRepository.findByGoogleId(sub).orElse(null);
        }

        if (user == null) {
            // LẦN ĐẦU: tạo user mới, set các trường từ Google
            user = new User();
            user.setEmail(email);
            user.setGoogleId(sub);
            user.setFullName(googleName); // chỉ set lần đầu
            user.setGivenName(givenName);
            user.setFamilyName(familyName);
            user.setPicture(picture);
            user.setVerifiedEmail(true);
            user.setProvider("google");
        } else {
            // ĐÃ TỒN TẠI: không overwrite các trường người dùng có thể tự sửa
            // Chỉ cập nhật các trường đồng bộ từ Google khi an toàn
            if (user.getGoogleId() == null && sub != null) {
                user.setGoogleId(sub);
            }
            user.setPicture(picture);
            user.setVerifiedEmail(true);
            user.setProvider("google");
        }

        userRepository.save(user);

        // Trả lại user gốc; có thể wrap thành CustomOAuth2User nếu cần thêm thuộc tính/role
        return oAuth2User;
    }

    // --- Helpers an toàn kiểu ---
    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : null;
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String)  return Boolean.valueOf((String) v);
        return null;
    }
}

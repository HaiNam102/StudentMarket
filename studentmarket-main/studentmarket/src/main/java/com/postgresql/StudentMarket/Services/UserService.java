package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.postgresql.StudentMarket.Entities.Role;
import java.util.Set;


import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User processOAuthPostLogin(String googleId, String email, String fullName, String picture, boolean verifiedEmail) {

        Optional<User> existUserOpt = userRepository.findByGoogleId(googleId);
        if (existUserOpt.isPresent()) {
            User existingUser = existUserOpt.get();
            existingUser.setFullName(fullName);
            existingUser.setPicture(picture);
            existingUser.setVerifiedEmail(verifiedEmail);
            return userRepository.save(existingUser);
        }

        // Nếu email tồn tại nhưng Google ID mới
        Optional<User> emailUserOpt = userRepository.findByEmail(email);
        if (emailUserOpt.isPresent()) {
            User existingEmailUser = emailUserOpt.get();
            existingEmailUser.setGoogleId(googleId);
            existingEmailUser.setFullName(fullName);
            existingEmailUser.setPicture(picture);
            existingEmailUser.setVerifiedEmail(verifiedEmail);
            return userRepository.save(existingEmailUser);
        }

        // Tạo user mới
User newUser = new User();
newUser.setGoogleId(googleId);
newUser.setEmail(email);
newUser.setFullName(fullName);
newUser.setPicture(picture);
newUser.setVerifiedEmail(verifiedEmail);
newUser.setProvider("GOOGLE");

// Gán role mặc định USER (role_id = 1)
Role userRole = new Role();
userRole.setRoleId(1); // hoặc lấy từ RoleRepository nếu bạn có
newUser.setRoles(Set.of(userRole));

return userRepository.save(newUser);

    }
}

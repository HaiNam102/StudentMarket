package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User validateAdmin(String username, String rawPassword) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null 
            && user.getRoles().stream().anyMatch(r -> "admin".equalsIgnoreCase(r.getRoleName()))
            && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }
}

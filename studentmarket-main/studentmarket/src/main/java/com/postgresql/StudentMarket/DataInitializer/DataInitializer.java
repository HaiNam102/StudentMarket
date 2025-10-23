package com.postgresql.StudentMarket.DataInitializer;

import com.postgresql.StudentMarket.Entities.Role;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Entities.UserRole;
import com.postgresql.StudentMarket.Repository.RoleRepository;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Nếu chưa có role admin thì tạo
        Role adminRole = roleRepository.findByRoleName("admin")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("admin");
                    return roleRepository.save(role);
                });

        // Nếu chưa có user admin thì tạo
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("12345"));
            userRepository.saveAndFlush(admin);

            // Gắn role cho user qua UserRole
            UserRole adminUserRole = new UserRole(admin, adminRole);
            userRoleRepository.save(adminUserRole);

            System.out.println("✅ Admin account created: username=admin, password=12345");
        }
    }
}

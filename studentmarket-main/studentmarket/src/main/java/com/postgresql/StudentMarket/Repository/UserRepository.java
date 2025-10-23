package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    // Đếm số role ADMIN của 1 user (JOIN user_role ↔ role)
    @Query(value = """
        SELECT COUNT(*)
        FROM user_role ur
        JOIN role r ON ur.role_id = r.role_id
        WHERE ur.user_id = :userId
          AND UPPER(r.role_name) = 'ADMIN'
        """, nativeQuery = true)
    long countAdminRoles(@Param("userId") Integer userId);

    // Tiện dụng: gọi thẳng userRepository.isAdmin(id)
    default boolean isAdmin(Integer userId) {
        return userId != null && countAdminRoles(userId) > 0;
    }
}

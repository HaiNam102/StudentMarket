package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "picture")
    private String picture;

    @Column(name = "verified_email")
    private boolean verifiedEmail;

    @Column(name = "provider")
    private String provider; // google / local

    // ========== Dùng cho Local Login ==========
    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password; // Mật khẩu băm (BCrypt)

    // ========== Trường hồ sơ người dùng ==========
    @Column(name = "gender", length = 10)
    private String gender; // MALE | FEMALE | OTHER

    @Column(name = "phone", length = 10)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "date_of_birth") // cột DATE trong DB
    private LocalDate dateOfBirth;

    @Column(name = "joined_at")
    private LocalDate joinedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    // ========== Quan hệ với Role ==========
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles;

    public String getSdt() {
        throw new UnsupportedOperationException("Unimplemented method 'getSdt'");
    }

}

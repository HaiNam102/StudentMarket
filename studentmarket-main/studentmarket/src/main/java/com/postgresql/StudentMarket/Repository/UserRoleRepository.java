package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.UserRole;
import com.postgresql.StudentMarket.Entities.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}

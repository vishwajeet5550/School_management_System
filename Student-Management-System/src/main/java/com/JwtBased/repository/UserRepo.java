// UserRepository.java
package com.JwtBased.repository;

import com.JwtBased.entity.User;
import com.JwtBased.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    long countByRoleAndIsActive(Role role, Boolean isActive);
}
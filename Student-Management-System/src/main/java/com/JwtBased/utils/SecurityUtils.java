package com.JwtBased.utils;

import com.JwtBased.entity.User;
import com.JwtBased.enums.Role;
import com.JwtBased.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepo userRepository;

    // ── Get Current Authentication ─────────────────────────
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // ── Get Current User ID ────────────────────────────────
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user.getId();
    }

    // ── Get Current Username ───────────────────────────────
    public String getCurrentUsername() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        return auth.getName();
    }

    // ── Get Current User (Full Entity) ────────────────────
    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    // ── Get Current Role ───────────────────────────────────
    public Role getCurrentRole() {
        return getCurrentUser().getRole();
    }

    // ── Role Check Helpers ─────────────────────────────────
    public boolean isAdmin() {
        return getCurrentRole() == Role.ADMIN;
    }

    public boolean isTeacher() {
        return getCurrentRole() == Role.TEACHER;
    }

    public boolean isStudent() {
        return getCurrentRole() == Role.STUDENT;
    }

    // ── Check if user is accessing own data ───────────────
    public boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }
}
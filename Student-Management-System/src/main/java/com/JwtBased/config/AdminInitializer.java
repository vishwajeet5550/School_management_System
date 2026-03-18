package com.JwtBased.config;

import com.JwtBased.entity.User;
import com.JwtBased.enums.Role;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.existsByUsername("admin")) {
            System.out.println("✅ Admin already exists, skipping...");
            return;
        }

        User adminUser = User.builder()
                .username("admin")
                .email("admin@school.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .fullName("System Administrator")
                .phone("9999999999")
                .department("Administration")
                .isActive(true)
                .createdBy("SYSTEM")
                .build();

        userRepository.save(adminUser);
        System.out.println("✅ Admin created → username: admin | password: admin123");
    }
}
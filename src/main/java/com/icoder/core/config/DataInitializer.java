package com.icoder.core.config;

import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@RequiredArgsConstructor
@Configuration
public class DataInitializer {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

            User admin = User.builder()
                    .handle("roaazz")
                    .nickname("Roaa Mohamed")
                    .email("roaaamohamed66@gmail.com")
                    .password(passwordEncoder.encode("Password@123"))
                    .verified(true)
                    .createdAt(Instant.now().toString())
                    .lastVerificationEmailSentAt(Instant.now().toString())
                    .build();

            if (!userRepository.existsByHandle(admin.getHandle())) {
                userRepository.save(admin);
                System.out.println("User created successfully.");
            }
            else {
                System.out.println("User already exists.");
            }
        };
    }
}

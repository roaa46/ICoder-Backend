package com.icoder.core.config;

import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class DataInitializer {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info(">>>> Start Initializing");

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
                log.info("User created successfully.");
            }
            else {
                log.warn("User already exists.");
            }
        };
    }
}

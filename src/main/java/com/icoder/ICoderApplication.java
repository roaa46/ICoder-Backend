package com.icoder;

import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@RequiredArgsConstructor
@SpringBootApplication
public class ICoderApplication {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(ICoderApplication.class, args);
    }

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
                System.out.println("Admin created successfully.");
            }
        };
    }
}

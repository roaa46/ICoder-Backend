package com.icoder;

import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
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
        Dotenv dotenv = Dotenv.load();

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("TOKEN_EXPIRATION", dotenv.get("TOKEN_EXPIRATION"));
        System.setProperty("REFRESH_TOKEN_EXPIRATION", dotenv.get("REFRESH_TOKEN_EXPIRATION"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("EMAIL", dotenv.get("EMAIL"));
        System.setProperty("PASSWORD", dotenv.get("PASSWORD"));
        System.setProperty("UPLOAD_DIR", dotenv.get("UPLOAD_DIR"));

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

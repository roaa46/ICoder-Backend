package com.icoder.core.config;

import com.icoder.user.management.dto.auth.RegisterRequest;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.mapper.UserMapper;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class DataInitializer {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info(">>>> Start Initializing");

            // Define initial users
            List<RegisterRequest> initialUsers = List.of(
                    createUserRequest("user1", "User One", "saaameh.0.1@gmail.com", "Password@123"),
                    createUserRequest("user2", "User Two", "samshx606@gmail.com", "Password@123"),
                    createUserRequest("user3", "User Three", "samshx404@gmail.com", "Password@123")
            );

            // Create users
            initialUsers.forEach(this::createUserIfNotExists);

            log.info(">>>> Initialization Complete");
        };
    }

    private RegisterRequest createUserRequest(String handle, String nickname, String email, String password) {
        return RegisterRequest.builder()
                .handle(handle)
                .nickname(nickname)
                .email(email)
                .password(password)
                .passwordConfirmation(password)
                .build();
    }

    private void createUserIfNotExists(RegisterRequest request) {
        if (userRepository.existsByHandle(request.getHandle())) {
            log.info("User '{}' already exists, skipping.", request.getHandle());
            return;
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(true);
        userRepository.save(user);
        log.info("User '{}' created successfully.", request.getHandle());
    }
}

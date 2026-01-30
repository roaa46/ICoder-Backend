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

            RegisterRequest request = RegisterRequest.builder()
                    .handle("roaazz")
                    .nickname("Roaa Mohamed")
                    .email("roaaamohamed66@gmail.com")
                    .password(passwordEncoder.encode("Password@123"))
                    .passwordConfirmation(passwordEncoder.encode("Password@123"))
                    .build();
            User user = userMapper.toEntity(request);
            user.setVerified(true);
            if (!userRepository.existsByHandle(user.getHandle())) {
                userRepository.save(user);
                log.info("User created successfully.");
            } else {
                log.warn("User already exists.");
            }
        };
    }
}

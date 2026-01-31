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
            log.info(">>>> Start Initializing Data...");

            List<RegisterRequest> usersToCreate = List.of(
                    createRequest("roaazz", "roaa mohamed", "roaaamohamed66@gmail.com"),
                    createRequest("saam_03", "sam", "samshx404@gmail.com"),
                    createRequest("roaa", "roaa2", "roaamohamedd60@gmail.com")
            );

            usersToCreate.forEach(this::saveUserIfNotExists);

            log.info(">>>> Data Initialization Finished.");
        };
    }

    private RegisterRequest createRequest(String handle, String nickname, String email) {
        String encodedPassword = passwordEncoder.encode("Password@123");
        return RegisterRequest.builder()
                .handle(handle)
                .nickname(nickname)
                .email(email)
                .password(encodedPassword)
                .passwordConfirmation(encodedPassword)
                .build();
    }

    private void saveUserIfNotExists(RegisterRequest request) {
        if (!userRepository.existsByHandle(request.getHandle())) {
            User user = userMapper.toEntity(request);
            user.setVerified(true);
            userRepository.save(user);
            log.info("User [{}] created successfully.", request.getHandle());
        } else {
            log.warn("User [{}] already exists. Skipping...", request.getHandle());
        }
    }
}
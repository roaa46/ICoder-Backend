package com.icoder.core.config;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.icoder.user.management.dto.auth.RegisterRequest;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.mapper.UserMapper;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class DataInitializer {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BotAccountRepository botAccountRepository;

    @Value("${cses.handle}")
    private String csesHandle;

    @Value("${cses.password}")
    private String csesPassword;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info(">>>> Start Initializing Data...");

            List<RegisterRequest> usersToCreate = List.of(
                    createRequest("roaazz", "roaa mohamed", "roaaamohamed66@gmail.com"),
                    createRequest("saam_03", "sam", "samshx404@gmail.com"),
                    createRequest("roaa", "roaa2", "roaamohamedd60@gmail.com"),
                    createRequest("saif","Saif wael","sw1132000@gmail.com")
            );

            usersToCreate.forEach(this::saveUserIfNotExists);
            initializeCsesBotAccount();

            log.info(">>>> Data Initialization Finished.");
        };
    }

    private void initializeCsesBotAccount() {
        try {
            if (csesHandle == null || csesHandle.isEmpty()) {
                log.warn("CSES Bot credentials are not provided in properties.");
                return;
            }

            if (!botAccountRepository.existsByUsernameAndJudgeType(csesHandle, OJudgeType.CSES)) {
                BotAccount botAccount = BotAccount.builder()
                        .username(csesHandle)
                        .password(csesPassword)
                        .judgeType(OJudgeType.CSES)
                        .active(true)
                        .inUse(false)
                        .lastUsedAt(Instant.now())
                        .build();

                botAccountRepository.save(botAccount);
                log.info("Successfully created CSES Bot Account: [{}]", csesHandle);
            } else {
                log.info("CSES Bot Account [{}] already exists.", csesHandle);
            }
        } catch (Exception e) {
            log.error("Failed to create CSES Bot Account. Error: {}", e.getMessage());
        }
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
        try {
            if (!userRepository.existsByHandle(request.getHandle())) {
                User user = userMapper.toEntity(request);
                user.setVerified(true);
                userRepository.save(user);
                log.info("Successfully created user: [{}]", request.getHandle());
            } else {
                log.warn("Skipping: User [{}] already exists.", request.getHandle());
            }
        } catch (Exception e) {
            log.error("Failed to create user [{}]. Error: {}", request.getHandle(), e.getMessage());
        }
    }
}
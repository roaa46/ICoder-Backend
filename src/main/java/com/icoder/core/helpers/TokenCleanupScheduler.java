package com.icoder.core.helpers;

import com.icoder.user.management.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 2 * * ?") //runs every day at 2 AM
    public void cleanOldTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = tokenRepository.deleteAllByCreatedAtBefore(cutoff);
        System.out.println("Deleted " + deleted + " old tokens");
    }
}

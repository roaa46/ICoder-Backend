package com.icoder.core.utils;

import com.icoder.user.management.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 2 * * ?") //runs every day at 2 AM
    public void cleanOldTokens() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        int deleted = tokenRepository.deleteAllByCreatedAtBefore(cutoff);
        System.out.println("Deleted " + deleted + " old tokens");
    }
}

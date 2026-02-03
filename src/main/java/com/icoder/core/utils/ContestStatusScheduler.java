package com.icoder.core.utils;

import com.icoder.contest.management.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContestStatusScheduler {

    private final ContestRepository contestRepository;

    @Scheduled(fixedRate = 60000)
    public void updateContestStatuses() {
        Instant now = Instant.now();

        log.info("Running contest status update task at {}", now);

        contestRepository.startScheduledContests(now);

        contestRepository.endRunningContests(now);
    }
}

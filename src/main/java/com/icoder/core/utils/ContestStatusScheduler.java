package com.icoder.core.utils;

import com.icoder.contest.management.dto.ContestStatusChangedEvent;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContestStatusScheduler {

    private final ContestRepository contestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void updateContestStatuses() {
        Instant now = Instant.now();

        List<Long> toStart = contestRepository.findIdsToStart(now);
        if (!toStart.isEmpty()) {
            contestRepository.startContestsByIds(toStart);
            toStart.forEach(id ->
                    eventPublisher.publishEvent(new ContestStatusChangedEvent(id, ContestStatus.RUNNING))
            );
            log.info("Contests started: {}", toStart);
        }

        List<Long> toEnd = contestRepository.findIdsToEnd(now);
        if (!toEnd.isEmpty()) {
            contestRepository.endContestsByIds(toEnd);
            toEnd.forEach(id ->
                    eventPublisher.publishEvent(new ContestStatusChangedEvent(id, ContestStatus.ENDED))
            );
            log.info("Contests ended: {}", toEnd);
        }
    }
}

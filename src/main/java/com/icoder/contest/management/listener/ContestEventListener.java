package com.icoder.contest.management.listener;

import com.icoder.contest.management.dto.ContestStatusChangedEvent;
import com.icoder.core.service.implementaion.GeneralStreamServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContestEventListener {

    private final GeneralStreamServiceImpl streamService;

    @EventListener
    public void handleContestStatusChange(ContestStatusChangedEvent event) {
        log.info("Contest {} status changed to {}", event.contestId(), event.newStatus());
        streamService.broadcast("CONTEST_UPDATE", event);
    }
}
package com.icoder.submission.management.listener;

import com.icoder.submission.management.events.SubmissionUpdatedEvent;
import com.icoder.submission.management.service.implementation.SubmissionStreamServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionEventListener {

    private final SubmissionStreamServiceImpl streamService;

    @EventListener
    public void handleSubmissionUpdate(SubmissionUpdatedEvent event) {
        streamService.sendUpdate(event.submissionId(), event.verdict());
    }
}
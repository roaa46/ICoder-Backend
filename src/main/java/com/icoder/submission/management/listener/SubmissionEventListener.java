package com.icoder.submission.management.listener;

import com.icoder.core.service.implementaion.GeneralStreamServiceImpl;
import com.icoder.submission.management.events.SubmissionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionEventListener {

    private final GeneralStreamServiceImpl streamService;

    @EventListener
    public void handleSubmissionUpdate(SubmissionUpdatedEvent event) {
        streamService.sendToUser(event.userId(), "SUBMISSION_RESULT", event);
    }
}
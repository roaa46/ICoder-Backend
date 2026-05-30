package com.icoder.submission.management.listener;

import com.icoder.core.config.RabbitMQConfig;
import com.icoder.submission.management.service.implementation.ResultCheckerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubmissionTaskConsumer {

    private final ResultCheckerService resultCheckerService;

    @RabbitListener(queues = RabbitMQConfig.SUBMISSION_QUEUE)
    public void handleCheckTask(Long submissionId) {
        log.info("Received check task for submission ID: {}", submissionId);
        try {
            resultCheckerService.checkSingleSubmission(submissionId);
        } catch (Exception e) {
            log.error("Error processing submission check for ID {}: {}", submissionId, e.getMessage());
        }
    }
}
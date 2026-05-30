package com.icoder.submission.management.service.implementation;

import com.icoder.core.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCheckTask(Long submissionId) {
        log.info("Publishing check task for submission ID: {}", submissionId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SUBMISSION_DELAY_QUEUE, submissionId);
    }
}
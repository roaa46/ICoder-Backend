package com.icoder.submission.management.service.implementation;

import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.utils.SubmissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionProcessor {
    private final SubmissionManagerService submissionManagerService;
    private final SubmissionUtils submissionUtils;

    @Async("threadPoolTaskExecutor")
    public void process(Long submissionId, SubmissionCreateRequest request) {
        log.info("Queuing submission ID: {} using CompletableFuture", submissionId);
        try {
            submissionManagerService.initAndProcess(submissionId, request);
            log.info("Submission {} processed successfully.", submissionId);
        } catch (Exception ex) {
            log.error("Critical error in submission pipeline for ID {}: {}", submissionId, ex.getMessage());
            submissionUtils.handleFailure(submissionId);
        }
    }
}

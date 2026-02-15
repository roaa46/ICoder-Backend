package com.icoder.submission.management.service.implementation;

import com.icoder.submission.management.utils.SubmissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionProcessor {
    private final SubmissionManagerService submissionManagerService;
    private final SubmissionUtils submissionUtils;

    public void process(Long submissionId) {
        log.info("Queuing submission ID: {} using CompletableFuture", submissionId);

        CompletableFuture.runAsync(() -> submissionManagerService.initAndProcess(submissionId))
                .thenRun(() -> log.info("Submission {} processed successfully.", submissionId))
                .exceptionally(ex -> {
                    log.error("Critical error in submission pipeline for ID {}: {}", submissionId, ex.getMessage());
                    submissionUtils.handleFailure(submissionId);
                    return null;
                });
    }
}

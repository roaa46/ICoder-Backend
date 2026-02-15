package com.icoder.submission.management.service.interfaces;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SubmissionStreamService {
    SseEmitter createEmitter(Long submissionId);
    void sendUpdate(Long submissionId, Object data);
}

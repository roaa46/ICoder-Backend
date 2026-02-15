package com.icoder.submission.management.service.implementation;

import com.icoder.submission.management.service.interfaces.SubmissionStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SubmissionStreamServiceImpl implements SubmissionStreamService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitter(Long submissionId) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(5));

        emitter.onCompletion(() -> emitters.remove(submissionId));
        emitter.onTimeout(() -> emitters.remove(submissionId));
        emitter.onError((e) -> emitters.remove(submissionId));

        emitters.put(submissionId, emitter);
        return emitter;
    }

    @Override
    public void sendUpdate(Long submissionId, Object data) {
        SseEmitter emitter = emitters.get(submissionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("submission-update")
                        .data(data));
                log.info("Sent SSE update for submission {}", submissionId);
            } catch (IOException e) {
                log.warn("Client disconnected for submission {}, removing emitter", submissionId);
                emitters.remove(submissionId);
            }
        }
    }
}

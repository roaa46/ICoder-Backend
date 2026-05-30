package com.icoder.core.service.implementaion;

import com.icoder.core.service.interfaces.GeneralStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GeneralStreamServiceImpl implements GeneralStreamService {
    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(30));

        emitter.onCompletion(() -> userEmitters.remove(userId));
        emitter.onTimeout(() -> userEmitters.remove(userId));
        emitter.onError((e) -> userEmitters.remove(userId));

        userEmitters.put(userId, emitter);
        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                userEmitters.remove(userId);
            }
        }
    }

    public void broadcast(String eventName, Object data) {
        userEmitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                userEmitters.remove(userId);
            }
        });
    }
}

package com.icoder.core.service.interfaces;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface GeneralStreamService {
    SseEmitter createEmitter(Long userId);
}

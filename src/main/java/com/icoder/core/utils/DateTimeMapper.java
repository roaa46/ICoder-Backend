package com.icoder.core.utils;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class DateTimeMapper {

    // it's used in request
    public Duration toDuration(String length) {
        if (length == null || !length.contains(":")) return Duration.ZERO;
        try {
            String[] parts = length.split(":");
            return Duration.ofHours(Long.parseLong(parts[0]))
                    .plusMinutes(Long.parseLong(parts[1]))
                    .plusSeconds(Long.parseLong(parts[2]));
        } catch (Exception e) {
            return Duration.ZERO;
        }
    }

    // it's used in response
    public String fromDuration(Duration duration) {
        if (duration == null) return "00:00:00";
        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    // it's used in request'
    public Instant toInstant(String beginTime) {
        if (beginTime == null || beginTime.isBlank()) {
            return null;
        }
        return Instant.parse(beginTime);
    }
}

package com.icoder.notification.management.dto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.notification.management.enums.NotificationType;
import lombok.Data;

import java.time.Instant;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationResponse {
    private Long id;
    private String message;
    private NotificationType type;
    private String actionUrl;
    private boolean isRead;
    private Long targetId;
    private Instant createdAt;
}
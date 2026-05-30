package com.icoder.notification.management.mapper;

import com.icoder.notification.management.dto.NotificationResponse;
import com.icoder.notification.management.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toDTO(Notification notification);
}

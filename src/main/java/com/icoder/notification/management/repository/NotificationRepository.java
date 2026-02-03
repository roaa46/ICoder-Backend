package com.icoder.notification.management.repository;

import com.icoder.notification.management.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository <Notification, Long>{
    Page <Notification> findAllByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
}

package com.retailpulse.notification.repository;

import com.retailpulse.notification.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByOrderId(String orderId);

    long countByChannelAndStatus(NotificationChannel channel, NotificationStatus status);
}

package com.retailpulse.notification.dto;

import com.retailpulse.notification.model.*;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long                id,
    NotificationChannel channel,
    NotificationStatus  status,
    String              recipient,
    String              subject,
    String              orderId,
    String              failureReason,
    LocalDateTime       createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
            n.getId(), n.getChannel(), n.getStatus(),
            n.getRecipient(), n.getSubject(), n.getOrderId(),
            n.getFailureReason(), n.getCreatedAt());
    }
}

package com.retailpulse.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
    @NotBlank(message = "Recipient is required")
    String recipient,

    String subject,
    String message,
    String orderId
) {}

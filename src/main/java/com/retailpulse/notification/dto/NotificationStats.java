package com.retailpulse.notification.dto;

public record NotificationStats(
    long totalSent,
    long totalFailed,
    long emailSent,
    long emailFailed,
    long smsSent,
    long smsFailed
) {}

package com.retailpulse.notification.service;

import com.retailpulse.notification.dto.*;
import com.retailpulse.notification.model.*;
import com.retailpulse.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    /**
     * Configurable simulated failure rate.
     * Set notification.failure-rate in application.yml (default 10 %).
     */
    @Value("${notification.failure-rate:10}")
    private int failureRatePercent;

    private final Random random = new Random();

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationResponse send(NotificationChannel channel, NotificationRequest request) {

        boolean delivered = random.nextInt(100) >= failureRatePercent;

        if (delivered) {
            log.info("[{}] SENT to {} | order={}", channel, request.recipient(), request.orderId());
        } else {
            log.warn("[{}] FAILED to {} | order={}", channel, request.recipient(), request.orderId());
        }

        Notification notification = Notification.builder()
                .channel(channel)
                .status(delivered ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .recipient(request.recipient())
                .subject(request.subject())
                .message(request.message())
                .orderId(request.orderId())
                .failureReason(delivered ? null : "Simulated delivery failure")
                .build();

        return NotificationResponse.from(notificationRepository.save(notification));
    }

    /**
     * Convenience method called directly by OrderService — no HTTP hop.
     */
    public void sendOrderConfirmation(String orderId, String customerId,
                                       String productCode, double amount) {
        send(NotificationChannel.EMAIL, new NotificationRequest(
            customerId + "@retailpulse.com",
            "Order Confirmed: " + orderId,
            String.format("Your order %s for %s (%.2f) has been confirmed.", orderId, productCode, amount),
            orderId
        ));
    }

    /**
     * Convenience method called directly by OrderService — no HTTP hop.
     */
    public void sendOrderFailure(String orderId, String customerId, String reason) {
        send(NotificationChannel.SMS, new NotificationRequest(
            customerId,
            "Order Failed",
            String.format("Order %s could not be placed: %s", orderId, reason),
            orderId
        ));
    }

    public NotificationStats getStats() {
        long emailSent   = notificationRepository.countByChannelAndStatus(
                               NotificationChannel.EMAIL, NotificationStatus.SENT);
        long emailFailed = notificationRepository.countByChannelAndStatus(
                               NotificationChannel.EMAIL, NotificationStatus.FAILED);
        long smsSent     = notificationRepository.countByChannelAndStatus(
                               NotificationChannel.SMS, NotificationStatus.SENT);
        long smsFailed   = notificationRepository.countByChannelAndStatus(
                               NotificationChannel.SMS, NotificationStatus.FAILED);

        return new NotificationStats(
            emailSent + smsSent,
            emailFailed + smsFailed,
            emailSent, emailFailed,
            smsSent, smsFailed
        );
    }
}

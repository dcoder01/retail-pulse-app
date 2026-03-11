package com.retailpulse.notification.controller;

import com.retailpulse.common.ApiResponse;
import com.retailpulse.notification.dto.*;
import com.retailpulse.notification.model.NotificationChannel;
import com.retailpulse.notification.model.NotificationStatus;
import com.retailpulse.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendEmail(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.send(NotificationChannel.EMAIL, request);
        HttpStatus status = response.status() == NotificationStatus.SENT
                ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ApiResponse.ok(response));
    }

    @PostMapping("/sms")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendSms(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.send(NotificationChannel.SMS, request);
        HttpStatus status = response.status() == NotificationStatus.SENT
                ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ApiResponse.ok(response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<NotificationStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getStats()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "module", "notification"));
    }
}

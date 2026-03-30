package com.campus.booking.controller;

import com.campus.booking.model.Notification;
import com.campus.booking.model.User;
import com.campus.booking.repository.NotificationRepository;
import com.campus.booking.repository.UserRepository;
import com.campus.booking.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService    notificationService;
    @Autowired private UserRepository         userRepository;
    @Autowired private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            List<Notification> list =
                    notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            List<Map<String, Object>> result = list.stream().map(n -> Map.<String, Object>of(
                "id",        n.getId(),
                "message",   n.getMessage(),
                "isRead",    Boolean.TRUE.equals(n.getIsRead()),
                "createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""
            )).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("GET /notifications error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            notificationService.markAllRead(user.getId());
        } catch (Exception e) {
            System.err.println("mark-read error: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
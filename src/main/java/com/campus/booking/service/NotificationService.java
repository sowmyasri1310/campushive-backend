package com.campus.booking.service;

import com.campus.booking.model.Booking;
import com.campus.booking.model.Notification;
import com.campus.booking.model.User;
import com.campus.booking.repository.NotificationRepository;
import com.campus.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository         userRepository;

    // Send with a booking reference
    public void sendToUser(User user, Booking booking, String message) {
        saveWithBooking(user, booking, message);
    }

    // Send WITHOUT a booking reference (used after booking is deleted)
    public void sendToUserById(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElse(null);
        if (user != null) saveNoBooking(user, message);
    }

    // Notify all admins of a branch (with booking ref)
    public void sendToAdminsOfBranch(String branch, Booking booking, String message) {
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .filter(u -> branch != null && branch.equalsIgnoreCase(u.getBranch()))
                .forEach(admin -> saveWithBooking(admin, booking, message));
    }

    // Notify matching students (with booking ref)
    public void sendToMatchingStudents(Booking booking, String message,
                                       String branch, String section) {
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .filter(u -> matchesBranchSection(u, branch, section))
                .forEach(s -> saveWithBooking(s, booking, message));
    }

    // Notify matching students WITHOUT booking ref (used after booking deleted)
    public void sendToMatchingStudentsByBranchSection(String message,
                                                       String branch, String section) {
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .filter(u -> matchesBranchSection(u, branch, section))
                .forEach(s -> saveNoBooking(s, message));
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAllRead(Long userId) {
        List<Notification> list =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean matchesBranchSection(User u, String branch, String section) {
        if (branch == null || branch.isEmpty() || branch.equals("ALL")) return true;
        boolean branchMatch = branch.equalsIgnoreCase(u.getBranch());
        if (section == null || section.isEmpty() || section.equals("ALL")) return branchMatch;
        return branchMatch && section.equalsIgnoreCase(u.getSection());
    }

    // Save with booking FK
    private void saveWithBooking(User user, Booking booking, String message) {
        Notification n = Notification.builder()
                .user(user).booking(booking)
                .message(message).isRead(false)
                .build();
        notificationRepository.save(n);
    }

    // Save WITHOUT booking FK (booking already deleted)
    private void saveNoBooking(User user, String message) {
        Notification n = Notification.builder()
                .user(user).booking(null)
                .message(message).isRead(false)
                .build();
        notificationRepository.save(n);
    }
}
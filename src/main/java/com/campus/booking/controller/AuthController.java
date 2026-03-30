package com.campus.booking.controller;

import com.campus.booking.dto.LoginRequest;
import com.campus.booking.dto.RegisterRequest;
import com.campus.booking.model.Booking;
import com.campus.booking.model.User;
import com.campus.booking.repository.BookingRepository;
import com.campus.booking.repository.NotificationRepository;
import com.campus.booking.repository.UserRepository;
import com.campus.booking.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private NotificationRepository notificationRepository;

    // ───────────────── LOGIN ─────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(authService.login(req));
        } catch (RuntimeException e) {
            return bad(e);
        }
    }

    // ───────────────── REGISTER ─────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            return ResponseEntity.ok(authService.register(req));
        } catch (RuntimeException e) {
            return bad(e);
        }
    }

    // ───────────────── SUPER ADMIN CREATE ADMIN ─────────────────

    @PostMapping("/super-admin/create-admin")
    public ResponseEntity<?> createAdmin(
            @RequestBody RegisterRequest req,
            @AuthenticationPrincipal UserDetails ud) {

        try {
            return ResponseEntity.ok(
                authService.superAdminCreateAdmin(req, resolve(ud))
            );
        } catch (RuntimeException e) {
            return bad(e);
        }
    }

    // ───────────────── SUPER ADMIN LIST ADMINS ─────────────────

    @GetMapping("/super-admin/admins")
    public ResponseEntity<?> listAdmins(
            @AuthenticationPrincipal UserDetails ud) {

        User caller = resolve(ud);

        if (caller.getRole() != User.Role.SUPER_ADMIN)
            return ResponseEntity.status(403)
                    .body(Map.of("message","Super Admin only"));

        return ResponseEntity.ok(
                userRepository.findByRoleOrderByBranchAscNameAsc(User.Role.ADMIN)
        );
    }

    // ───────────────── SUPER ADMIN DELETE ADMIN (FIX) ─────────────────

    @DeleteMapping("/super-admin/admins/{id}")
    @Transactional
    public ResponseEntity<?> deleteAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {

        try {
            User caller = resolve(ud);

            if (caller.getRole() != User.Role.SUPER_ADMIN)
                return ResponseEntity.status(403)
                        .body(Map.of("message","Super Admin only"));

            User target = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            if (target.getRole() != User.Role.ADMIN)
                return ResponseEntity.status(403)
                        .body(Map.of("message","Can only delete branch admins"));

            userRepository.delete(target);

            return ResponseEntity.ok(Map.of(
                    "message", "Admin \"" + target.getName() + "\" removed successfully"
            ));

        } catch (RuntimeException e) {
            return bad(e);
        }
    }

    // ───────────────── ADMIN CREATE FACULTY ─────────────────

    @PostMapping("/admin/create-faculty")
    public ResponseEntity<?> createFaculty(
            @RequestBody RegisterRequest req,
            @AuthenticationPrincipal UserDetails ud) {

        try {
            return ResponseEntity.ok(
                authService.adminCreateFaculty(req, resolve(ud))
            );
        } catch (RuntimeException e) {
            return bad(e);
        }
    }

    // ───────────────── ADMIN LIST FACULTY ─────────────────

    @GetMapping("/admin/faculty")
    public ResponseEntity<?> listFaculty(
            @AuthenticationPrincipal UserDetails ud) {

        User caller = resolve(ud);

        if (caller.getRole() != User.Role.ADMIN)
            return ResponseEntity.status(403)
                    .body(Map.of("message","Admins only"));

        return ResponseEntity.ok(
                userRepository.findByBranchAndRoleOrderByNameAsc(
                        caller.getBranch(),
                        User.Role.FACULTY
                )
        );
    }

    // ───────────────── ADMIN LIST STUDENTS ─────────────────

    @GetMapping("/admin/students")
    public ResponseEntity<?> listStudents(
            @AuthenticationPrincipal UserDetails ud) {

        User caller = resolve(ud);

        if (caller.getRole() != User.Role.ADMIN)
            return ResponseEntity.status(403)
                    .body(Map.of("message","Admins only"));

        return ResponseEntity.ok(
                userRepository.findByBranchAndRoleOrderByNameAsc(
                        caller.getBranch(),
                        User.Role.STUDENT
                )
        );
    }

    // ───────────────── ADMIN DELETE USER ─────────────────

    @DeleteMapping("/admin/users/{userId}")
    @Transactional
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails ud) {

        try {

            User admin = resolve(ud);

            if (admin.getRole() != User.Role.ADMIN)
                return ResponseEntity.status(403)
                        .body(Map.of("message","Admins only"));

            User target = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!admin.getBranch().equalsIgnoreCase(target.getBranch()))
                return ResponseEntity.status(403)
                        .body(Map.of("message","You can only delete users in your branch"));

            if (target.getRole() == User.Role.ADMIN ||
                target.getRole() == User.Role.SUPER_ADMIN)
                return ResponseEntity.status(403)
                        .body(Map.of("message","Cannot delete admin accounts"));

            notificationRepository.deleteByUserId(userId);

            if (target.getRole() == User.Role.FACULTY) {
                List<Booking> bookings =
                        bookingRepository.findByFacultyIdOrderByCreatedAtDesc(userId);

                for (Booking b : bookings) {
                    notificationRepository.deleteByBookingId(b.getId());
                }

                bookingRepository.deleteAll(bookings);
            }

            userRepository.delete(target);

            return ResponseEntity.ok(Map.of(
                    "message", target.getRole() + " \"" + target.getName() + "\" deleted successfully"
            ));

        } catch (RuntimeException e) {
            return bad(e);
        }
    }

    // ───────────────── HELPER METHODS ─────────────────

    private User resolve(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ResponseEntity<?> bad(RuntimeException e) {
        System.err.println("AUTH ERROR: " + e.getMessage()); // ✅ ADD THIS
        e.printStackTrace(); // ✅ ADD THIS (VERY IMPORTANT)
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
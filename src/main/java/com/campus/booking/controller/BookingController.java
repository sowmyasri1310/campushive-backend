package com.campus.booking.controller;

import com.campus.booking.dto.AdminActionRequest;
import com.campus.booking.dto.BookingRequest;
import com.campus.booking.model.Booking;
import com.campus.booking.model.User;
import com.campus.booking.repository.UserRepository;
import com.campus.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
    RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class BookingController {

    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        try   { return ResponseEntity.ok(bookingService.createBooking(req, ud.getUsername())); }
        catch (RuntimeException e) { return bad(e); }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Booking>> mine(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(bookingService.getFacultyBookings(ud.getUsername()));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending(@AuthenticationPrincipal UserDetails ud) {
        User admin = resolve(ud);
        if (admin.getRole() != User.Role.ADMIN)
            return ResponseEntity.status(403).body(Map.of("message","Admins only"));
        return ResponseEntity.ok(bookingService.getPendingBookings(admin.getBranch()));
    }

    @GetMapping("/all")
    public ResponseEntity<?> all(@AuthenticationPrincipal UserDetails ud) {
        User admin = resolve(ud);
        if (admin.getRole() != User.Role.ADMIN)
            return ResponseEntity.status(403).body(Map.of("message","Admins only"));
        return ResponseEntity.ok(bookingService.getAllBookings(admin.getBranch()));
    }

    @PatchMapping("/{id}/action")
    public ResponseEntity<?> action(@PathVariable Long id,
            @RequestBody AdminActionRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            User admin = resolve(ud);
            return ResponseEntity.ok(bookingService.processBooking(id, req, admin.getBranch()));
        } catch (RuntimeException e) { return bad(e); }
    }

    // Faculty requests cancellation of an approved booking
    @PatchMapping("/{id}/cancel-request")
    public ResponseEntity<?> cancelRequest(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            User faculty = resolve(ud);
            String reason = body.getOrDefault("reason", "");
            bookingService.requestCancellation(id, faculty, reason);
            return ResponseEntity.ok(Map.of("message", "Cancellation request sent to admin"));
        } catch (RuntimeException e) { return bad(e); }
    }

    // Faculty deletes own PENDING/REJECTED
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFaculty(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            User faculty = resolve(ud);
            bookingService.deleteBooking(id, faculty);
            return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
        } catch (RuntimeException e) { return bad(e); }
    }

    // Admin force-cancels any booking
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            User caller = resolve(ud);
            if (caller.getRole() != User.Role.ADMIN)
                return ResponseEntity.status(403).body(Map.of("message","Only admins can cancel classes"));
            bookingService.adminDeleteBooking(id, caller);
            return ResponseEntity.ok(Map.of("message", "Class cancelled successfully"));
        } catch (RuntimeException e) { return bad(e); }
    }

    @GetMapping("/student")
    public ResponseEntity<List<Booking>> student(@AuthenticationPrincipal UserDetails ud) {
        User user = resolve(ud);
        return ResponseEntity.ok(
                bookingService.getStudentBookings(user.getBranch(), user.getSection()));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Booking>> room(@PathVariable Long roomId) {
        return ResponseEntity.ok(bookingService.getRoomBookings(roomId));
    }

    private User resolve(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    private ResponseEntity<?> bad(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
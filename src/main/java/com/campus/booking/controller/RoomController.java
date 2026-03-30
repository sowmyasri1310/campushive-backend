package com.campus.booking.controller;

import com.campus.booking.model.Room;
import com.campus.booking.model.User;
import com.campus.booking.repository.BookingRepository;
import com.campus.booking.repository.NotificationRepository;
import com.campus.booking.repository.RoomRepository;
import com.campus.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RoomController {

    @Autowired private RoomRepository         roomRepository;
    @Autowired private UserRepository         userRepository;
    @Autowired private BookingRepository      bookingRepository;
    @Autowired private NotificationRepository notificationRepository;

    // Public — all roles see rooms
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomRepository.findByIsActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // SUPER_ADMIN — add a room
    @PostMapping
    public ResponseEntity<?> addRoom(@RequestBody Room room,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            // ✅ FIX: prevent null crash (main issue)
            if (ud == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Unauthorized"));
            }

            User caller = userRepository.findByEmail(ud.getUsername()).orElseThrow();

            if (caller.getRole() != User.Role.SUPER_ADMIN)
                return ResponseEntity.status(403).body(Map.of("message","Super Admin only"));

            room.setIsActive(true);
            Room saved = roomRepository.save(room);
            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {
            e.printStackTrace(); // ✅ DEBUG
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // SUPER_ADMIN — update a room
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id,
            @RequestBody Room updated,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            // ✅ FIX
            if (ud == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Unauthorized"));
            }

            User caller = userRepository.findByEmail(ud.getUsername()).orElseThrow();

            if (caller.getRole() != User.Role.SUPER_ADMIN)
                return ResponseEntity.status(403).body(Map.of("message","Super Admin only"));

            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            room.setName(updated.getName());
            room.setCapacity(updated.getCapacity());
            room.setType(updated.getType());
            room.setLocation(updated.getLocation());
            room.setAmenities(updated.getAmenities());
            room.setBlock(updated.getBlock());
            room.setFloor(updated.getFloor());
            room.setRoomNumber(updated.getRoomNumber());

            return ResponseEntity.ok(roomRepository.save(room));

        } catch (RuntimeException e) {
            e.printStackTrace(); // ✅ DEBUG
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // SUPER_ADMIN — delete a room
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteRoom(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            // ✅ FIX
            if (ud == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Unauthorized"));
            }

            User caller = userRepository.findByEmail(ud.getUsername()).orElseThrow();

            if (caller.getRole() != User.Role.SUPER_ADMIN)
                return ResponseEntity.status(403).body(Map.of("message","Super Admin only"));

            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            bookingRepository.findByRoomIdOrderByStartTimeAsc(id).forEach(b -> {
                notificationRepository.deleteByBookingId(b.getId());
                bookingRepository.delete(b);
            });

            roomRepository.delete(room);

            return ResponseEntity.ok(Map.of(
                "message", "Room \"" + room.getName() + "\" deleted successfully"
            ));

        } catch (RuntimeException e) {
            e.printStackTrace(); // ✅ DEBUG
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // SUPER_ADMIN — deactivate (soft delete)
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleRoom(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        try {
            // ✅ FIX
            if (ud == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Unauthorized"));
            }

            User caller = userRepository.findByEmail(ud.getUsername()).orElseThrow();

            if (caller.getRole() != User.Role.SUPER_ADMIN)
                return ResponseEntity.status(403).body(Map.of("message","Super Admin only"));

            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            room.setIsActive(!room.getIsActive());
            roomRepository.save(room);

            return ResponseEntity.ok(Map.of(
                "message", room.getName() + " is now " + (room.getIsActive() ? "ACTIVE" : "INACTIVE"),
                "isActive", room.getIsActive()
            ));

        } catch (RuntimeException e) {
            e.printStackTrace(); // ✅ DEBUG
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // All rooms including inactive (super admin view)
    @GetMapping("/all")
    public ResponseEntity<?> getAllRoomsAdmin(@AuthenticationPrincipal UserDetails ud) {

        // ✅ FIX
        if (ud == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Unauthorized"));
        }

        User caller = userRepository.findByEmail(ud.getUsername()).orElseThrow();

        if (caller.getRole() != User.Role.SUPER_ADMIN)
            return ResponseEntity.status(403).body(Map.of("message","Super Admin only"));

        return ResponseEntity.ok(roomRepository.findAll());
    }
}
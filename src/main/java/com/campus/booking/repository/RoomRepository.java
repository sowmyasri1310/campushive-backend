package com.campus.booking.repository;

import com.campus.booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ── Existing ─────────────────────────────────
    List<Room> findByIsActiveTrue();

    // ✅ NEW METHOD ADDED (sorting)
    List<Room> findByIsActiveTrueOrderByNameAsc();

    // ── Existing custom query ─────────────────────
    @Query("""
        SELECT r FROM Room r
        WHERE r.isActive = true
        AND r.id NOT IN (
            SELECT b.room.id FROM Booking b
            WHERE b.status = 'APPROVED'
            AND b.startTime < :endTime
            AND b.endTime   > :startTime
        )
    """)
    List<Room> findAvailableRooms(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime")   LocalDateTime endTime
    );
}
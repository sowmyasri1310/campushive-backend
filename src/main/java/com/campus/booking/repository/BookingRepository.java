package com.campus.booking.repository;

import com.campus.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);
    List<Booking> findByStatusOrderByCreatedAtDesc(Booking.BookingStatus status);
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByRoomIdOrderByStartTimeAsc(Long roomId);

    // Branch-scoped for admin
    List<Booking> findByFaculty_BranchOrderByCreatedAtDesc(String branch);
    List<Booking> findByFaculty_BranchAndStatusOrderByCreatedAtDesc(String branch, Booking.BookingStatus status);

    // Clash check for new booking
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.status = 'APPROVED' " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    long countApprovedClash(
        @Param("roomId")    Long roomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime")   LocalDateTime endTime
    );

    // Clash check when approving
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.id <> :excludeId " +
           "AND b.status = 'APPROVED' " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    long countApprovedClashExcluding(
        @Param("roomId")    Long roomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime")   LocalDateTime endTime,
        @Param("excludeId") Long excludeId
    );

    // Student view — only classes ending AFTER now AND starting BEFORE weekEnd (next 7 days)
    @Query("SELECT b FROM Booking b " +
           "WHERE b.status = 'APPROVED' " +
           "AND b.endTime > :now " +
           "AND b.startTime < :weekEnd " +
           "AND (b.branch = :branch OR b.branch = 'ALL' OR b.branch IS NULL) " +
           "AND (b.section = :section OR b.section = 'ALL' OR b.section IS NULL) " +
           "ORDER BY b.startTime ASC")
    List<Booking> findActiveApprovedForStudent(
        @Param("branch")   String branch,
        @Param("section")  String section,
        @Param("now")      LocalDateTime now,
        @Param("weekEnd")  LocalDateTime weekEnd
    );

    // Fallback — no branch filter, still next 7 days only
    @Query("SELECT b FROM Booking b " +
           "WHERE b.status = 'APPROVED' " +
           "AND b.endTime > :now " +
           "AND b.startTime < :weekEnd " +
           "ORDER BY b.startTime ASC")
    List<Booking> findAllActiveApproved(
        @Param("now")     LocalDateTime now,
        @Param("weekEnd") LocalDateTime weekEnd
    );

    // Room schedule — active upcoming only
    @Query("SELECT b FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.endTime > :now " +
           "ORDER BY b.startTime ASC")
    List<Booking> findActiveByRoomId(
        @Param("roomId") Long roomId,
        @Param("now")    LocalDateTime now
    );
}
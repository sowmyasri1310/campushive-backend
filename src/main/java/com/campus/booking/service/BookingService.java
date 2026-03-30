package com.campus.booking.service;

import com.campus.booking.dto.AdminActionRequest;
import com.campus.booking.dto.BookingRequest;
import com.campus.booking.model.Booking;
import com.campus.booking.model.Room;
import com.campus.booking.model.User;
import com.campus.booking.repository.BookingRepository;
import com.campus.booking.repository.NotificationRepository;
import com.campus.booking.repository.RoomRepository;
import com.campus.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired private BookingRepository      bookingRepository;
    @Autowired private RoomRepository         roomRepository;
    @Autowired private UserRepository         userRepository;
    @Autowired private NotificationService    notificationService;
    @Autowired private NotificationRepository notificationRepository;

    public Booking createBooking(BookingRequest request, String facultyEmail) {
        User faculty = userRepository.findByEmail(facultyEmail)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        LocalDateTime start = request.getStartTime();
        LocalDateTime end   = request.getEndTime();

        if (end.isBefore(LocalDateTime.now()))
            throw new RuntimeException("Cannot book a room for a past time slot.");
        if (!end.isAfter(start))
            throw new RuntimeException("End time must be after start time.");
        if (!start.toLocalDate().equals(end.toLocalDate()))
            throw new RuntimeException("⚠️ Start and end time must be on the same day.");
        if (Duration.between(start, end).toHours() > 8)
            throw new RuntimeException("⚠️ Booking duration cannot exceed 8 hours.");
        if (bookingRepository.countApprovedClash(room.getId(), start, end) > 0)
            throw new RuntimeException("⚠️ Room already has an APPROVED booking in this time slot.");

        Booking booking = Booking.builder()
                .room(room).faculty(faculty)
                .title(request.getTitle()).purpose(request.getPurpose())
                .branch(request.getBranch()).section(request.getSection())
                .startTime(start).endTime(end)
                .status(Booking.BookingStatus.PENDING)
                .build();
        bookingRepository.save(booking);

        String adminMsg = "📋 New booking request from " + faculty.getName()
                + " | Class: \"" + booking.getTitle() + "\""
                + " | Room: " + room.getName()
                + " | Date: " + start.toLocalDate()
                + " | Time: " + start.toLocalTime() + " → " + end.toLocalTime()
                + (booking.getBranch()  != null ? " | Branch: "  + booking.getBranch()  : "")
                + (booking.getSection() != null ? " | Section: " + booking.getSection() : "");
        notificationService.sendToAdminsOfBranch(faculty.getBranch(), booking, adminMsg);

        return booking;
    }

    // Faculty requests cancellation of an APPROVED booking
    public void requestCancellation(Long bookingId, User faculty, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getFaculty().getId().equals(faculty.getId()))
            throw new RuntimeException("You can only request cancellation for your own bookings");

        if (booking.getStatus() != Booking.BookingStatus.APPROVED)
            throw new RuntimeException("Only APPROVED bookings can have cancellation requested");

        if (Boolean.TRUE.equals(booking.getCancelRequested()))
            throw new RuntimeException("Cancellation already requested for this booking");

        booking.setCancelRequested(true);
        booking.setCancelReason(reason);
        bookingRepository.save(booking);

        // Notify admin
        String adminMsg = "⚠️ Cancellation request from " + faculty.getName()
                + " | Class: \"" + booking.getTitle() + "\""
                + " | Room: " + booking.getRoom().getName()
                + " | Date: " + booking.getStartTime().toLocalDate()
                + " | Time: " + booking.getStartTime().toLocalTime()
                + " → " + booking.getEndTime().toLocalTime()
                + " | Reason: " + (reason.isEmpty() ? "Not specified" : reason);
        notificationService.sendToAdminsOfBranch(faculty.getBranch(), booking, adminMsg);
    }

    // Faculty deletes own PENDING/REJECTED
    @Transactional
    public void deleteBooking(Long bookingId, User faculty) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getFaculty().getId().equals(faculty.getId()))
            throw new RuntimeException("You can only delete your own bookings");
        if (booking.getStatus() == Booking.BookingStatus.APPROVED)
            throw new RuntimeException("⚠️ Cannot delete an APPROVED booking. Please request cancellation.");
        notificationRepository.deleteByBookingId(bookingId);
        bookingRepository.delete(booking);
    }

    // Admin cancels any booking
    @Transactional
    public void adminDeleteBooking(Long bookingId, User admin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getFaculty().getBranch().equalsIgnoreCase(admin.getBranch()))
            throw new RuntimeException("You can only cancel bookings in your branch.");

        String title     = booking.getTitle();
        String roomName  = booking.getRoom().getName();
        String roomLoc   = booking.getRoom().getLocation();
        String date      = booking.getStartTime().toLocalDate().toString();
        String startTime = booking.getStartTime().toLocalTime().toString();
        String endTime   = booking.getEndTime().toLocalTime().toString();
        String branch    = booking.getBranch();
        String section   = booking.getSection();
        User   faculty   = booking.getFaculty();
        boolean wasApproved = booking.getStatus() == Booking.BookingStatus.APPROVED;

        notificationRepository.deleteByBookingId(bookingId);
        bookingRepository.delete(booking);
        bookingRepository.flush();

        String facultyMsg = "🚫 Your booking \"" + title + "\""
                + " for " + roomName + " on " + date + " at " + startTime
                + " has been CANCELLED by the admin.";
        notificationService.sendToUserById(faculty.getId(), facultyMsg);

        if (wasApproved) {
            String studentMsg = "❌ Class cancelled: \"" + title + "\""
                    + " | Room: " + roomName + " (" + roomLoc + ")"
                    + " | Date: " + date + " | Time: " + startTime + " → " + endTime
                    + " has been CANCELLED by the admin."
                    + (branch  != null ? " | Branch: "  + branch  : "")
                    + (section != null ? " | Section: " + section : "");
            notificationService.sendToMatchingStudentsByBranchSection(studentMsg, branch, section);
        }
    }

    public List<Booking> getFacultyBookings(String email) {
        User faculty = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByFacultyIdOrderByCreatedAtDesc(faculty.getId());
    }

    public List<Booking> getPendingBookings(String adminBranch) {
        return bookingRepository
                .findByFaculty_BranchAndStatusOrderByCreatedAtDesc(
                        adminBranch, Booking.BookingStatus.PENDING);
    }

    public List<Booking> getAllBookings(String adminBranch) {
        return bookingRepository.findByFaculty_BranchOrderByCreatedAtDesc(adminBranch);
    }

    public Booking processBooking(Long bookingId, AdminActionRequest request, String adminBranch) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getFaculty().getBranch().equalsIgnoreCase(adminBranch))
            throw new RuntimeException("You can only manage bookings in your branch");

        if (request.getStatus() == Booking.BookingStatus.APPROVED) {
            if (bookingRepository.countApprovedClashExcluding(
                    booking.getRoom().getId(),
                    booking.getStartTime(), booking.getEndTime(),
                    booking.getId()) > 0)
                throw new RuntimeException(
                    "⚠️ Cannot approve: another booking is already APPROVED for this slot.");
        }

        booking.setStatus(request.getStatus());
        booking.setAdminNote(request.getAdminNote());
        bookingRepository.save(booking);

        String facultyMsg = request.getStatus() == Booking.BookingStatus.APPROVED
            ? "✅ Your booking \"" + booking.getTitle() + "\" for "
              + booking.getRoom().getName()
              + " on " + booking.getStartTime().toLocalDate()
              + " at " + booking.getStartTime().toLocalTime() + " has been APPROVED."
            : "❌ Your booking \"" + booking.getTitle() + "\" for "
              + booking.getRoom().getName()
              + " was REJECTED. Reason: " + request.getAdminNote();
        notificationService.sendToUser(booking.getFaculty(), booking, facultyMsg);

        if (request.getStatus() == Booking.BookingStatus.APPROVED) {
            String studentMsg =
                "📚 New class scheduled: \"" + booking.getTitle() + "\""
                + " | Room: " + booking.getRoom().getName()
                + " (" + booking.getRoom().getLocation() + ")"
                + " | Date: " + booking.getStartTime().toLocalDate()
                + " | Time: " + booking.getStartTime().toLocalTime()
                + " → " + booking.getEndTime().toLocalTime()
                + " | Faculty: " + booking.getFaculty().getName()
                + (booking.getBranch()  != null ? " | Branch: "  + booking.getBranch()  : "")
                + (booking.getSection() != null ? " | Section: " + booking.getSection() : "");
            notificationService.sendToMatchingStudents(
                    booking, studentMsg, booking.getBranch(), booking.getSection());
        }
        return booking;
    }

    public List<Booking> getStudentBookings(String branch, String section) {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime weekEnd = now.plusDays(7);
        if (branch == null || branch.isEmpty())
            return bookingRepository.findAllActiveApproved(now, weekEnd);
        return bookingRepository.findActiveApprovedForStudent(branch, section, now, weekEnd);
    }

    public List<Booking> getRoomBookings(Long roomId) {
        return bookingRepository.findActiveByRoomId(roomId, LocalDateTime.now());
    }
}
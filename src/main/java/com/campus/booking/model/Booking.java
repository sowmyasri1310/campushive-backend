package com.campus.booking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;

    @Column(nullable = false)
    private String title;

    private String purpose;
    private String branch;
    private String section;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "admin_note")
    private String adminNote;

    // Faculty can request admin to cancel an approved booking
    @Column(name = "cancel_requested", nullable = false)
    private Boolean cancelRequested = false;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum BookingStatus { PENDING, APPROVED, REJECTED }

    // Getters
    public Long getId()                  { return id; }
    public Room getRoom()                { return room; }
    public User getFaculty()             { return faculty; }
    public String getTitle()             { return title; }
    public String getPurpose()           { return purpose; }
    public String getBranch()            { return branch; }
    public String getSection()           { return section; }
    public LocalDateTime getStartTime()  { return startTime; }
    public LocalDateTime getEndTime()    { return endTime; }
    public BookingStatus getStatus()     { return status; }
    public String getAdminNote()         { return adminNote; }
    public Boolean getCancelRequested()  { return cancelRequested; }
    public String getCancelReason()      { return cancelReason; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // Setters
    public void setId(Long id)                        { this.id = id; }
    public void setRoom(Room room)                    { this.room = room; }
    public void setFaculty(User faculty)              { this.faculty = faculty; }
    public void setTitle(String title)                { this.title = title; }
    public void setPurpose(String purpose)            { this.purpose = purpose; }
    public void setBranch(String branch)              { this.branch = branch; }
    public void setSection(String section)            { this.section = section; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime)     { this.endTime = endTime; }
    public void setStatus(BookingStatus status)       { this.status = status; }
    public void setAdminNote(String adminNote)        { this.adminNote = adminNote; }
    public void setCancelRequested(Boolean v)         { this.cancelRequested = v; }
    public void setCancelReason(String cancelReason)  { this.cancelReason = cancelReason; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Booking b = new Booking();
        public Builder room(Room v)               { b.room = v;        return this; }
        public Builder faculty(User v)            { b.faculty = v;     return this; }
        public Builder title(String v)            { b.title = v;       return this; }
        public Builder purpose(String v)          { b.purpose = v;     return this; }
        public Builder branch(String v)           { b.branch = v;      return this; }
        public Builder section(String v)          { b.section = v;     return this; }
        public Builder startTime(LocalDateTime v) { b.startTime = v;   return this; }
        public Builder endTime(LocalDateTime v)   { b.endTime = v;     return this; }
        public Builder status(BookingStatus v)    { b.status = v;      return this; }
        public Booking build()                    { return b; }
    }
}
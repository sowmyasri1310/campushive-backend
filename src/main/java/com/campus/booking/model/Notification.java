package com.campus.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore                          // ← don't serialize full user object
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore                          // ← don't serialize full booking object (causes lazy error)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read")
    @JsonProperty("isRead")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters
    public Long getId()                  { return id; }
    public User getUser()                { return user; }
    public Booking getBooking()          { return booking; }
    public String getMessage()           { return message; }
    @JsonProperty("isRead")
    public Boolean getIsRead()           { return isRead; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // Setters
    public void setId(Long id)                        { this.id = id; }
    public void setUser(User user)                    { this.user = user; }
    public void setBooking(Booking booking)           { this.booking = booking; }
    public void setMessage(String message)            { this.message = message; }
    public void setIsRead(Boolean isRead)             { this.isRead = isRead; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Notification n = new Notification();
        public Builder user(User u)       { n.user = u;    return this; }
        public Builder booking(Booking b) { n.booking = b; return this; }
        public Builder message(String m)  { n.message = m; return this; }
        public Builder isRead(Boolean r)  { n.isRead = r;  return this; }
        public Notification build()       { return n; }
    }
}
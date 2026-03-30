package com.campus.booking.dto;

import com.campus.booking.model.Booking;

public class AdminActionRequest {
    private Booking.BookingStatus status;
    private String adminNote;

    public Booking.BookingStatus getStatus()           { return status; }
    public void setStatus(Booking.BookingStatus status){ this.status = status; }
    public String getAdminNote()                       { return adminNote; }
    public void setAdminNote(String adminNote)         { this.adminNote = adminNote; }
}
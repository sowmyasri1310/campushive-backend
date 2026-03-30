package com.campus.booking.dto;

import java.time.LocalDateTime;

public class BookingRequest {
    private Long roomId;
    private String title;
    private String purpose;
    private String branch;
    private String section;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getRoomId()                       { return roomId; }
    public void setRoomId(Long roomId)            { this.roomId = roomId; }
    public String getTitle()                      { return title; }
    public void setTitle(String title)            { this.title = title; }
    public String getPurpose()                    { return purpose; }
    public void setPurpose(String purpose)        { this.purpose = purpose; }
    public String getBranch()                     { return branch; }
    public void setBranch(String branch)          { this.branch = branch; }
    public String getSection()                    { return section; }
    public void setSection(String section)        { this.section = section; }
    public LocalDateTime getStartTime()           { return startTime; }
    public void setStartTime(LocalDateTime start) { this.startTime = start; }
    public LocalDateTime getEndTime()             { return endTime; }
    public void setEndTime(LocalDateTime end)     { this.endTime = end; }
}
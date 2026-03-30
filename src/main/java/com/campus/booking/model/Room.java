package com.campus.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    private String location;
    private String amenities;

    // ✅ NEW FIELDS ADDED (without disturbing old code)
    private String block;
    private String floor;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum RoomType {
        CLASSROOM, LAB, SEMINAR_HALL, AUDITORIUM
    }

    // ── Getters ──────────────────────────────────
    public Long getId()           { return id; }
    public String getName()       { return name; }
    public Integer getCapacity()  { return capacity; }
    public RoomType getType()     { return type; }
    public String getLocation()   { return location; }
    public String getAmenities()  { return amenities; }
    public String getBlock()      { return block; }        // ✅ added
    public String getFloor()      { return floor; }        // ✅ added
    public String getRoomNumber() { return roomNumber; }   // ✅ added
    public Boolean getIsActive()  { return isActive; }

    // ── Setters ──────────────────────────────────
    public void setId(Long id)              { this.id = id; }
    public void setName(String name)        { this.name = name; }
    public void setCapacity(Integer c)      { this.capacity = c; }
    public void setType(RoomType type)      { this.type = type; }
    public void setLocation(String loc)     { this.location = loc; }
    public void setAmenities(String am)     { this.amenities = am; }
    public void setBlock(String block)      { this.block = block; }        // ✅ added
    public void setFloor(String floor)      { this.floor = floor; }        // ✅ added
    public void setRoomNumber(String rn)    { this.roomNumber = rn; }      // ✅ added
    public void setIsActive(Boolean active) { this.isActive = active; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Room room = new Room();

        public Builder id(Long id)             { room.id = id; return this; }
        public Builder name(String name)       { room.name = name; return this; }
        public Builder capacity(Integer c)     { room.capacity = c; return this; }
        public Builder type(RoomType t)        { room.type = t; return this; }
        public Builder location(String loc)    { room.location = loc; return this; }
        public Builder amenities(String am)    { room.amenities = am; return this; }

        // ✅ added to builder
        public Builder block(String block)     { room.block = block; return this; }
        public Builder floor(String floor)     { room.floor = floor; return this; }
        public Builder roomNumber(String rn)   { room.roomNumber = rn; return this; }

        public Builder isActive(Boolean active){ room.isActive = active; return this; }

        public Room build()                    { return room; }
    }
}
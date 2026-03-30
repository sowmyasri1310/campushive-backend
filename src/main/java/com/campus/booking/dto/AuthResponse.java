package com.campus.booking.dto;

import com.campus.booking.model.User;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private User.Role role;
    private Long id;
    private String branch;
    private String section;

    public AuthResponse(String token, String name, String email,
                        User.Role role, Long id, String branch, String section) {
        this.token   = token;
        this.name    = name;
        this.email   = email;
        this.role    = role;
        this.id      = id;
        this.branch  = branch;
        this.section = section;
    }

    public String getToken()    { return token; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public User.Role getRole()  { return role; }
    public Long getId()         { return id; }
    public String getBranch()   { return branch; }
    public String getSection()  { return section; }
}
package com.campus.booking.dto;

import com.campus.booking.model.User;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private User.Role role;
    private String department;
    private String branch;
    private String section;

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }
    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }
    public User.Role getRole()               { return role; }
    public void setRole(User.Role role)      { this.role = role; }
    public String getDepartment()            { return department; }
    public void setDepartment(String dept)   { this.department = dept; }
    public String getBranch()                { return branch; }
    public void setBranch(String branch)     { this.branch = branch; }
    public String getSection()               { return section; }
    public void setSection(String section)   { this.section = section; }
}
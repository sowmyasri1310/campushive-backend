package com.campus.booking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String department;
    private String branch;
    private String section;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum Role { STUDENT, FACULTY, ADMIN, SUPER_ADMIN }

    public Long getId()                  { return id; }
    public String getName()              { return name; }
    public String getEmail()             { return email; }
    public String getPassword()          { return password; }
    public Role getRole()                { return role; }
    public String getDepartment()        { return department; }
    public String getBranch()            { return branch; }
    public String getSection()           { return section; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setId(Long id)                        { this.id = id; }
    public void setName(String name)                  { this.name = name; }
    public void setEmail(String email)                { this.email = email; }
    public void setPassword(String password)          { this.password = password; }
    public void setRole(Role role)                    { this.role = role; }
    public void setDepartment(String department)      { this.department = department; }
    public void setBranch(String branch)              { this.branch = branch; }
    public void setSection(String section)            { this.section = section; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final User u = new User();
        public Builder name(String v)       { u.name = v;       return this; }
        public Builder email(String v)      { u.email = v;      return this; }
        public Builder password(String v)   { u.password = v;   return this; }
        public Builder role(Role v)         { u.role = v;       return this; }
        public Builder department(String v) { u.department = v; return this; }
        public Builder branch(String v)     { u.branch = v;     return this; }
        public Builder section(String v)    { u.section = v;    return this; }
        public User build()                 { return u; }
    }
}
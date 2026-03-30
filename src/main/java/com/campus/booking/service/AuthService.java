package com.campus.booking.service;

import com.campus.booking.dto.AuthResponse;
import com.campus.booking.dto.LoginRequest;
import com.campus.booking.dto.RegisterRequest;
import com.campus.booking.model.User;
import com.campus.booking.repository.UserRepository;
import com.campus.booking.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    // ───────────── LOGIN ─────────────
    public AuthResponse login(LoginRequest request) {

        // ✅ FIX: null / trim safety
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String password = request.getPassword();

        if (email == null || email.isEmpty())
            throw new RuntimeException("Email is required");

        if (password == null || password.isEmpty())
            throw new RuntimeException("Password is required");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ IMPORTANT: debug (remove later if needed)
        System.out.println("LOGIN ATTEMPT: " + email);

        // ✅ FIX: proper password check (already correct, kept same)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return toResponse(jwtUtil.generateToken(user.getEmail()), user);
    }

    // ───────────── REGISTER ─────────────
    public AuthResponse register(RegisterRequest request) {

        // ✅ FIX: trim email
        String email = request.getEmail() != null ? request.getEmail().trim() : null;

        if (email == null || email.isEmpty())
            throw new RuntimeException("Email is required");

        if (userRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // ✅ correct
                .role(User.Role.STUDENT)
                .department(request.getDepartment())
                .branch(request.getBranch())
                .section(request.getSection())
                .build();

        userRepository.save(user);

        return toResponse(jwtUtil.generateToken(user.getEmail()), user);
    }

    // ───────────── SUPER ADMIN CREATE ADMIN ─────────────
    public User superAdminCreateAdmin(RegisterRequest req, User caller) {

        assertRole(caller, User.Role.SUPER_ADMIN, "Only Super Admin can create branch admins");

        if (req.getBranch() == null || req.getBranch().isBlank())
            throw new RuntimeException("Branch is required for admin accounts");

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail().trim()) // ✅ FIX
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.ADMIN)
                .department(req.getDepartment())
                .branch(req.getBranch())
                .section(null)
                .build();

        return userRepository.save(user);
    }

    // ───────────── ADMIN CREATE FACULTY ─────────────
    public User adminCreateFaculty(RegisterRequest req, User caller) {

        assertRole(caller, User.Role.ADMIN, "Only branch admins can create faculty accounts");

        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail().trim()) // ✅ FIX
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.FACULTY)
                .department(req.getDepartment())
                .branch(caller.getBranch())
                .section(null)
                .build();

        return userRepository.save(user);
    }

    private void assertRole(User caller, User.Role required, String msg) {
        if (caller == null || caller.getRole() != required)
            throw new RuntimeException(msg);
    }

    private AuthResponse toResponse(String token, User user) {
        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getId(),
                user.getBranch(),
                user.getSection()
        );
    }
}
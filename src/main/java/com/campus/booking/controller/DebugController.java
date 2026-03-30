package com.campus.booking.controller;

import com.campus.booking.model.User;
import com.campus.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DebugController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/test-login")
    public Map<String, Object> testLogin(
            @RequestParam String email,
            @RequestParam String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty())
            return Map.of("step","FAILED","reason","User not found");
        User user = userOpt.get();
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        return Map.of(
            "step",            "USER FOUND",
            "email",           user.getEmail(),
            "role",            user.getRole().name(),
            "passwordMatches", matches,
            "verdict",         matches ? "✅ LOGIN SHOULD WORK" : "❌ HASH MISMATCH"
        );
    }

    @GetMapping("/hash")
    public Map<String, String> generateHash(@RequestParam String plain) {
        String hash = passwordEncoder.encode(plain);
        return Map.of("plain", plain, "hash", hash);
    }

    // Test if auth header is received
    @GetMapping("/auth-check")
    public Map<String, Object> authCheck(
            @RequestHeader(value="Authorization", required=false) String authHeader) {
        return Map.of(
            "authHeaderPresent", authHeader != null,
            "authHeader", authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "MISSING"
        );
    }
}
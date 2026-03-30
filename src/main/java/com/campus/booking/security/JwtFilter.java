package com.campus.booking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsServiceImpl userDetailsService;

    private static final java.util.Set<String> PUBLIC_PATHS = java.util.Set.of(
        "/api/auth/login",
        "/api/auth/register"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path   = request.getServletPath();

        // OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ FIX: REMOVE rooms from skip
        if (PUBLIC_PATHS.contains(path) ||
            path.startsWith("/api/debug/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // JWT processing
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtUtil.extractUsername(token);

                if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails ud = userDetailsService.loadUserByUsername(email);

                    if (jwtUtil.validateToken(token, ud)) {

                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                ud, null, ud.getAuthorities());

                        authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }

            } catch (Exception e) {
                System.err.println("JWT error [" + method + " " + path + "]: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️ No JWT token for: " + method + " " + path);
        }

        filterChain.doFilter(request, response);
    }
}
package com.datn.datn_vanh.Security;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
        @Autowired
        private JwtUtil jwtUtil;
    private static final List<String> WHITELIST = List.of(
            "/auth/login",
            "/auth/register",
            "/recogni/recognition-stream"
    );



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (WHITELIST.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.validateToken(token);
            if (email != null) {
                try {
                    // Lấy role từ Firestore
                    Firestore db = FirestoreClient.getFirestore();
                    DocumentSnapshot userDoc = db.collection("Users").document(email).get().get();

                    if (userDoc.exists()) {
                        String role = userDoc.getString("role");
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        filterChain.doFilter(request, response);
    }


}

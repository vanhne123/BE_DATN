package com.datn.datn_vanh.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email,String role) {
        return Jwts.builder()
                .setSubject(email) // Đặt subject là email
                .claim("role", role) // Thêm role vào token
                .setIssuedAt(new Date()) // Thời gian cấp token
                .setExpiration(new Date(System.currentTimeMillis() + 900000 )) // Token hết hạn sau 15 phút
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sử dụng key để ký token
                .compact();
    }

    public String validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}

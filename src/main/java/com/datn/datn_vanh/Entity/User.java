package com.datn.datn_vanh.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.cloud.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String email;
    private String password;
    private String fullName;
    private String role;
    private Timestamp createdAt;
}


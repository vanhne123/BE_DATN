package com.datn.datn_vanh.Controller;

import com.datn.datn_vanh.Dto.Auth.LoginDto;
import com.datn.datn_vanh.Dto.Auth.RegisterDto;
import com.datn.datn_vanh.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthControllerImp implements AuthController {
    @Autowired
    private AuthService authService;


    public ResponseEntity<?> register(@RequestBody RegisterDto request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    public ResponseEntity<?> login(@RequestBody LoginDto request) {
        try {
            String token = authService.login(request);
            if (token == null) return ResponseEntity.status(401).body("Email hoặc mật khẩu sai");
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đăng nhập");
        }
    }
}
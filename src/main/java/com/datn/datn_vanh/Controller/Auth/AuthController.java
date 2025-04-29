package com.datn.datn_vanh.Controller.Auth;

import com.datn.datn_vanh.Dto.Auth.LoginDto;
import com.datn.datn_vanh.Dto.Auth.RegisterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthController {

    /*
    API for register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto request);

    /*
    API for login
    */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto request);

}

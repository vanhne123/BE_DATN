package com.datn.datn_vanh.Service;

import com.datn.datn_vanh.Dto.Auth.LoginDto;
import com.datn.datn_vanh.Dto.Auth.RegisterDto;
import com.datn.datn_vanh.Entity.Token;
import com.datn.datn_vanh.Entity.User;
import com.datn.datn_vanh.Security.JwtUtil;
import com.google.api.core.ApiFuture;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final Firestore db = FirestoreClient.getFirestore();
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(RegisterDto request) throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("Users")
                .whereEqualTo("email", request.getEmail()).get();

        if (!future.get().isEmpty()) {
            throw new RuntimeException("Email đã tồn tại.");
        }

        User newUser = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getRole() == null ? "USER" : request.getRole().toUpperCase(),
                Timestamp.now()
        );

        db.collection("Users").document(request.getEmail()).set(newUser);
        logger.info("Tạo tài khoản thành công" + request.getEmail());
        return "Tạo tài khoản thành công";
    }

    public String login(LoginDto request) throws Exception {
        ApiFuture<QuerySnapshot> future = db.collection("Users")
                .whereEqualTo("email", request.getEmail()).get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        if (docs.isEmpty()) return null;

        User user = docs.get(0).toObject(User.class);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) return null;


        String token = jwtUtil.generateToken(request.getEmail());

        // Save token to Firestore in a collection named "tokens"
        Token tokenData =  Token.builder()
                .token(token)
                .email(request.getEmail())
                .createdAt(Timestamp.now())
                .build();

        db.collection("tokens").add(tokenData);
        logger.info("Create token successfully with email: " + request.getEmail());

        return token;
    }
}

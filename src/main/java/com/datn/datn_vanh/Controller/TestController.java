package com.datn.datn_vanh.Controller;

import com.datn.datn_vanh.Service.RecognitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final RecognitionService recognitionService;

    // Inject RecognitionService vào Controller
    public TestController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @GetMapping("/protected")
    public ResponseEntity<String> testProtected() {
        String currentUser = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok("Hello, " + currentUser + "! You have accessed a protected API.");
    }

    // API mới để test lấy dữ liệu từ Firebase Realtime Database
    @GetMapping("/recognitions")
    public ResponseEntity<Object> getRecognitionsFromFirebase() {
        try {
            // Lấy dữ liệu từ Firebase thông qua CompletableFuture
            CompletableFuture<Object> future = recognitionService.getAllRecognitions();

            // Chờ kết quả từ CompletableFuture (sẽ không chặn luồng)
            Object recognitionsData = future.get(); // Hoặc future.join() nếu bạn muốn tránh exception

            if (recognitionsData != null) {
                return ResponseEntity.ok(recognitionsData); // Trả về dữ liệu nếu có
            } else {
                return ResponseEntity.status(404).body("No recognitions found.");
            }
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error retrieving recognitions data: " + e.getMessage());
        }
    }
}

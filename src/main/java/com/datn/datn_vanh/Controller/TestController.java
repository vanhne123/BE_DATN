package com.datn.datn_vanh.Controller;

import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import com.datn.datn_vanh.Service.RecognitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
            CompletableFuture<Object> future = recognitionService.getAllRecognitions();
            Object rawData = future.join();

            // Ép kiểu dữ liệu thành Map<String, Map<String, Map<String, Object>>>
            Map<String, Map<String, Map<String, Object>>> rootMap = (Map<String, Map<String, Map<String, Object>>>) rawData;
            List<EmployeeDto> resultList = new ArrayList<>();

            ObjectMapper objectMapper = new ObjectMapper();

            for (Map<String, Map<String, Object>> dateEntries : rootMap.values()) {
                for (Map<String, Object> record : dateEntries.values()) {
                    EmployeeDto dto = objectMapper.convertValue(record, EmployeeDto.class);
                    resultList.add(dto);
                }
            }

            return ResponseEntity.ok(resultList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving recognitions: " + e.getMessage());
        }
    }

}

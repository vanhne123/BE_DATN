package com.datn.datn_vanh.Controller.Recognitions;

import com.datn.datn_vanh.Dto.Recognition.RecognitionDto;
import com.datn.datn_vanh.Dto.Recognition.TotalChamCong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface RecognitionController {
    @GetMapping("/getAll")
    public List<RecognitionDto> getRecognitionsFromFirebase();

    @GetMapping("/getEmployeeRecogni")
    public List<RecognitionDto> getEmployeeRecogni(@RequestParam String employeeId);

    // Đăng ký kết nối SSE
    @GetMapping("/recognition-stream")
    public SseEmitter streamRecognitions();

    @GetMapping("/getRecogniByMonthandYear")
    public TotalChamCong filterByMonthAndYear(@RequestParam String id, @RequestParam String targetMonth, @RequestParam String targetYear);
}

package com.datn.datn_vanh.Controller.Recognitions;

import com.datn.datn_vanh.Dto.Recognition.CountChamCongDto;
import com.datn.datn_vanh.Dto.Recognition.RecognitionDto;
import com.datn.datn_vanh.Dto.Recognition.TotalChamCong;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/recognition-stream")
    public SseEmitter streamRecognitions();

    @GetMapping("/getRecogniByMonthandYear")
    public List<TotalChamCong> filterByMonthAndYear(@RequestParam String targetMonth, @RequestParam String targetYear);

    @GetMapping("/getCount")
    public List<CountChamCongDto> CountChamCong(@RequestParam String targetMonth, @RequestParam String targetYear);

    @GetMapping("/getCount_export")
    public ResponseEntity<ByteArrayResource> exportChamCongToExcel(@RequestParam String targetMonth, @RequestParam String targetYear);
}

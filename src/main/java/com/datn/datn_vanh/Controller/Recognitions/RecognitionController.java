package com.datn.datn_vanh.Controller.Recognitions;

import com.datn.datn_vanh.Dto.Recognition.RecognitionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecognitionController {
    @GetMapping("/getAll")
    public List<RecognitionDto> getRecognitionsFromFirebase();

    @GetMapping("/getEmployeeRecogni")
    public List<RecognitionDto> getEmployeeRecogni(@RequestParam String employeeId);
}

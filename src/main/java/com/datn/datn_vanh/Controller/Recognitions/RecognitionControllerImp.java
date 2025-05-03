package com.datn.datn_vanh.Controller.Recognitions;

import com.datn.datn_vanh.Dto.Recognition.RecognitionDto;
import com.datn.datn_vanh.Security.JwtUtil;
import com.datn.datn_vanh.Service.RecognitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recogni")
public class RecognitionControllerImp implements RecognitionController{

    private final RecognitionService recognitionService;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public RecognitionControllerImp(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @Override
    public List<RecognitionDto> getRecognitionsFromFirebase() {
        CompletableFuture<Object> future = recognitionService.getAllRecognitions();
        List<RecognitionDto> resultList = new ArrayList<>();

        try {
            // Đợi CompletableFuture hoàn thành và lấy dữ liệu
            Object rawData = future.join();

            // Kiểm tra kiểu dữ liệu của rawData trước khi ép kiểu
            if (rawData instanceof Map<?, ?>) {
                Map<String, Map<String, Object>> rootMap = (Map<String, Map<String, Object>>) rawData;
                ObjectMapper objectMapper = new ObjectMapper();

                for (Map.Entry<String, Map<String, Object>> entry : rootMap.entrySet()) {
                    String recognitionId = entry.getKey();  // ID nhận diện (có thể là employeeId hoặc ID khác)
                    Map<String, Object> dataEntry = entry.getValue(); // Dữ liệu nhận diện

                    // Duyệt qua các mục timestamp bên trong Map
                    for (Map.Entry<String, Object> timestampEntry : dataEntry.entrySet()) {
                        String timestamp = timestampEntry.getKey();  // Timestamp
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                        LocalDateTime dateTime = LocalDateTime.parse(timestamp, formatter);

                        // Chuyển LocalDateTime thành Timestamp
                        Timestamp timestampConverted = Timestamp.valueOf(dateTime);
                        Map<String, String> imageEntry = (Map<String, String>) timestampEntry.getValue();

                        if (imageEntry != null && imageEntry.containsKey("image_url")) {
                            String imageUrl = imageEntry.get("image_url");

                            // Chuyển đổi thành RecognitionDto
                            RecognitionDto recognitionDto = RecognitionDto.builder()
                                    .id(Long.parseLong(recognitionId))  // ID nhận diện
                                    .url(imageUrl)  // URL của hình ảnh
                                    .created(String.valueOf(timestampConverted))  // Timestamp
                                    .build();

                            resultList.add(recognitionDto);
                        } else {
                            logger.warn("Không tìm thấy image_url cho timestamp: {}", timestamp);
                        }
                    }
                }
            } else {
                logger.error("Dữ liệu nhận được không phải là kiểu Map hợp lệ.");
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý dữ liệu nhận diện", e);
        }


        return resultList.stream()
                .sorted(Comparator.comparing(RecognitionDto::getCreated).reversed())
                .collect(Collectors.toList());

    }

    @Override
    public List<RecognitionDto> getEmployeeRecogni(String employeeId) {
        CompletableFuture<Object> future = recognitionService.getEmployeeRecogniById(employeeId);
        Object rawData = future.join();
        List<RecognitionDto> resultList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        // Ép kiểu an toàn
        if (rawData instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) rawData;

            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String timestampStr = entry.getKey();
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                    Timestamp created = Timestamp.valueOf(dateTime);

                    Map<String, String> valueMap = (Map<String, String>) entry.getValue();
                    String imageUrl = valueMap.get("image_url");

                    if (imageUrl != null) {
                        RecognitionDto dto = RecognitionDto.builder()
                                .id(Long.parseLong(employeeId))
                                .url(imageUrl)
                                .created(String.valueOf(created))
                                .build();
                        resultList.add(dto);
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi khi parse timestamp {}: {}", timestampStr, e.getMessage());
                }
            }
        } else {
            logger.warn("Dữ liệu không đúng định dạng Map<String, Object>: {}", rawData);
        }

        // Sắp xếp giảm dần theo thời gian created
        resultList.sort((a, b) -> b.getCreated().compareTo(a.getCreated()));

        return resultList;
    }
}

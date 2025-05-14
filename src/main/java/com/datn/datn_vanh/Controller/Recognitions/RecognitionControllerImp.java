package com.datn.datn_vanh.Controller.Recognitions;

import com.datn.datn_vanh.Controller.Employee.EmployeeControllerImp;
import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import com.datn.datn_vanh.Dto.Recognition.CountChamCongDto;
import com.datn.datn_vanh.Dto.Recognition.RecognitionData;
import com.datn.datn_vanh.Dto.Recognition.RecognitionDto;
import com.datn.datn_vanh.Dto.Recognition.TotalChamCong;
import com.datn.datn_vanh.ENUM.Reference;
import com.datn.datn_vanh.Entity.Employee;
import com.datn.datn_vanh.Security.JwtUtil;
import com.datn.datn_vanh.Service.RecognitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recogni")
public class RecognitionControllerImp implements RecognitionController {

    private final RecognitionService recognitionService;
    private final EmployeeControllerImp employeeControllerImp;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    // Lưu trữ timestamp của các recognition đã nhận diện
//    private final Map<String, Set<String>> seenTimestamps = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 100;  // Giới hạn số lượng sự kiện trong cache
    private final ConcurrentLinkedQueue<String> eventCache = new ConcurrentLinkedQueue<>();
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public RecognitionControllerImp(RecognitionService recognitionService,EmployeeControllerImp  employeeControllerImp) {
        this.recognitionService = recognitionService;
        this.employeeControllerImp = employeeControllerImp;
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
    // Gửi lại các sự kiện đã cache khi client reconnect
    private void sendCachedEventsOnReconnect(SseEmitter emitter) {
        for (String event : eventCache) {
            try {
                emitter.send(SseEmitter.event().name("recognition").data(event, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                // Xử lý khi có lỗi gửi sự kiện, có thể là mất kết nối
                removeEmitter(emitter);
                logger.info(e.getMessage());
            }
        }
    }

    // Lưu sự kiện vào cache
    private void cacheEvent(String employeeId, String timestamp, String imageUrl) {
        String event = String.format("{\"employeeId\":\"%s\",\"timestamp\":\"%s\",\"imageUrl\":\"%s\"}", employeeId, timestamp, imageUrl);

        // Nếu cache đã đầy, xóa sự kiện cũ nhất
        if (eventCache.size() >= MAX_CACHE_SIZE) {
            eventCache.poll();  // Loại bỏ sự kiện cũ nhất
        }

        eventCache.add(event);  // Thêm sự kiện mới vào cache
    }

    // API để client kết nối
    @Override
    public SseEmitter streamRecognitions() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onTimeout(() -> removeEmitter(emitter));

        // Gửi lại các sự kiện đã cache khi client reconnect
        sendCachedEventsOnReconnect(emitter);

        return emitter;
    }
    @Scheduled(fixedRate = 1000)
    public void sendHeartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                removeEmitter(emitter);
            }
        }
    }

    // Xóa emitter khỏi danh sách khi có lỗi hoặc khi kết nối hoàn tất
    private void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    // Gửi sự kiện nhận diện mới tới tất cả các client
    public void sendRecognitionEvent(String employeeId, String timestamp, String imageUrl) {
        // Lưu sự kiện vào cache
        cacheEvent(employeeId, timestamp, imageUrl);

        // Gửi sự kiện tới tất cả các emitter đang kết nối
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("recognition").data(String.format("{\"employeeId\":\"%s\",\"timestamp\":\"%s\",\"imageUrl\":\"%s\"}", employeeId, timestamp, imageUrl), MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                // Xử lý lỗi khi không thể gửi sự kiện, loại bỏ emitter khỏi danh sách
                removeEmitter(emitter);
            }
        }
    }

    private String lastSentTimestamp = "";

    @Scheduled(fixedRate = 1000)
    public void checkNewRecognitions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Reference.RECOGNITIONS_PATH);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<RecognitionData> recognitions = new ArrayList<>();

                for (DataSnapshot employeeSnapshot : snapshot.getChildren()) {
                    String employeeId = employeeSnapshot.getKey();

                    for (DataSnapshot timeSnap : employeeSnapshot.getChildren()) {
                        String timestamp = timeSnap.getKey();
                        String imageUrl = (String) timeSnap.child("image_url").getValue();

                        // Chỉ lấy bản ghi mới
                        if (timestamp.compareTo(lastSentTimestamp) > 0) {
                            recognitions.add(new RecognitionData(employeeId, timestamp, imageUrl));
                        }
                    }
                }

                // Sắp xếp tăng dần rồi gửi
                recognitions.sort(Comparator.comparing(RecognitionData::getTimestamp));
                for (RecognitionData data : recognitions) {
                    sendRecognitionEvent(data.getEmployeeId(), data.getTimestamp(), data.getImageUrl());
                    lastSentTimestamp = data.getTimestamp(); // Cập nhật sau khi gửi
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Database error: " + error.getMessage());
            }
        });
    }


    @Override
    public List<TotalChamCong> filterByMonthAndYear(String targetMonth, String targetYear) {
        List<EmployeeDto> employees = employeeControllerImp.getAllEmployee(); // Lấy tất cả nhân viên
        List<TotalChamCong> result = new ArrayList<>();

        for (EmployeeDto employee : employees) {
            // Kiểm tra nếu id của nhân viên không phải là -1
            if (!employee.getId().equals("-1")) {
                // Lấy danh sách RecognitionDto cho nhân viên này
                List<RecognitionDto> recognitions = getEmployeeRecogni(String.valueOf(employee.getId()));

                // Lọc các recognition theo tháng và năm
                List<RecognitionDto> filteredList = recognitions.stream()
                        .filter(r -> {
                            try {
                                LocalDateTime dateTime = LocalDateTime.parse(r.getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
                                return dateTime.getMonthValue() == Integer.parseInt(targetMonth)
                                        && dateTime.getYear() == Integer.parseInt(targetYear);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());

                // Tính toán tổng công và tổng lương cho nhân viên
                Float totalCong = filteredList.size() / 2.0f; // Hoặc (float) filteredList.size() / 2
                Float baseSalary = 500000f; // Mức lương cơ bản cho mỗi ngày công
                Float salaryLevel = Float.parseFloat(employee.getSalary_level()); // Hệ số lương
                Float daysInMonth = 22f; // Giả sử 22 ngày làm việc trong tháng
                Float bonus = 100000f; // Thưởng cố định
                Float deductions = 150000f; // Giảm trừ (bảo hiểm, thuế, v.v.)

                // Tính lương với công thức đã sửa
                Float totalLuong = (totalCong ) * baseSalary * salaryLevel/10 + bonus - deductions;
                if (totalLuong < 0) {
                    totalLuong = 0f;
                }

                // Tạo DTO Dulieu cho từng nhân viên
                TotalChamCong dulieu = TotalChamCong.builder()
                        .id(String.valueOf(employee.getId()))
                        .name(employee.getName())
                        .danhSach(filteredList)
                        .totalCong(totalCong)
                        .totalLuong(totalLuong)
                        .build();


                result.add(dulieu);
            }
        }

        return result; // Trả về danh sách các đối tượng TotalChamCong
    }

    @Override
    public List<CountChamCongDto> CountChamCong(String targetMonth, String targetYear) {
        List<EmployeeDto> employees = employeeControllerImp.getAllEmployee(); // Lấy tất cả nhân viên
        List<CountChamCongDto> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        LocalTime standardStartTime = LocalTime.of(8, 30); // Giờ làm mặc định 08:30

        for (EmployeeDto employee : employees) {
            List<RecognitionDto> recognitions = getEmployeeRecogni(String.valueOf(employee.getId()));

            // Lọc các recognition theo tháng và năm
            List<RecognitionDto> filteredList = recognitions.stream()
                    .filter(r -> {
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(r.getCreated(), formatter);
                            return dateTime.getMonthValue() == Integer.parseInt(targetMonth)
                                    && dateTime.getYear() == Integer.parseInt(targetYear);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            // Tính tổng công
            Float totalCong = filteredList.size() / 2.0f;

            // Tính số phút đi muộn
            Map<LocalDate, LocalDateTime> earliestCheckinPerDay = new HashMap<>();
            for (RecognitionDto r : filteredList) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(r.getCreated(), formatter);
                    LocalDate date = dateTime.toLocalDate();
                    earliestCheckinPerDay.merge(date, dateTime,
                            (oldVal, newVal) -> newVal.isBefore(oldVal) ? newVal : oldVal);
                } catch (Exception e) {
                    // Bỏ qua nếu lỗi
                }
            }

            long totalLateMinutes = 0;
            for (LocalDateTime checkInTime : earliestCheckinPerDay.values()) {
                LocalTime actualTime = checkInTime.toLocalTime();
                if (actualTime.isAfter(standardStartTime)) {
                    totalLateMinutes += Duration.between(standardStartTime, actualTime).toMinutes();
                }
            }

            // Tạo DTO kết quả
            CountChamCongDto dulieu = CountChamCongDto.builder()
                    .id(String.valueOf(employee.getId()))
                    .name(employee.getName())
                    .danhSach(filteredList)
                    .totalCong(totalCong)
                    .lateMinutes((float) totalLateMinutes)
                    .build();

            result.add(dulieu);
        }

        return result;
    }


    public ResponseEntity<ByteArrayResource> exportChamCongToExcel(String targetMonth, String targetYear) {
        List<CountChamCongDto> data = CountChamCong(targetMonth, targetYear); // Gọi lại hàm tính toán đã có
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("Tổng hợp chấm công");
        Sheet sheet2 = workbook.createSheet("Chi tiết chấm công");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

        // ===== Sheet 1: Tổng hợp chấm công =====
        Row header1 = sheet1.createRow(0);
        String[] headers1 = {"STT", "Mã NV", "Tên NV", "Tổng công", "Tổng phút đi muộn"};
        for (int i = 0; i < headers1.length; i++) {
            header1.createCell(i).setCellValue(headers1[i]);
        }

        AtomicInteger rowIdx1 = new AtomicInteger(1);
        AtomicInteger stt1 = new AtomicInteger(1);
        for (CountChamCongDto dto : data) {
            Row row = sheet1.createRow(rowIdx1.getAndIncrement());
            row.createCell(0).setCellValue(stt1.getAndIncrement());
            row.createCell(1).setCellValue(dto.getId());
            row.createCell(2).setCellValue(dto.getName());
            row.createCell(3).setCellValue(dto.getTotalCong());
            row.createCell(4).setCellValue(dto.getLateMinutes());
        }

        // ===== Sheet 2: Chi tiết chấm công =====
        Row header2 = sheet2.createRow(0);
        String[] headers2 = {"STT", "Mã NV", "Tên NV", "Ngày", "Giờ chấm công"};
        for (int i = 0; i < headers2.length; i++) {
            header2.createCell(i).setCellValue(headers2[i]);
        }

        AtomicInteger rowIdx2 = new AtomicInteger(1);
        AtomicInteger stt2 = new AtomicInteger(1);
        for (CountChamCongDto dto : data) {
            for (RecognitionDto r : dto.getDanhSach()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(r.getCreated(), formatter);
                    Row row = sheet2.createRow(rowIdx2.getAndIncrement());
                    row.createCell(0).setCellValue(stt2.getAndIncrement());
                    row.createCell(1).setCellValue(dto.getId());
                    row.createCell(2).setCellValue(dto.getName());
                    row.createCell(3).setCellValue(dateTime.toLocalDate().toString());
                    row.createCell(4).setCellValue(dateTime.toLocalTime().toString());
                } catch (Exception e) {
                    // Bỏ qua nếu lỗi định dạng
                }
            }
        }

        // ===== Xuất file ra ByteArrayOutputStream =====
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ===== Trả về file cho người dùng =====
        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ChamCong_" + timestamp + ".xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(resource);
    }


}

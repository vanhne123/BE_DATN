package com.datn.datn_vanh.Dto.Recognition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountChamCongDto {

    private String id;
    private String name;// Định nghĩa id với kiểu dữ liệu String
    private List<RecognitionDto> danhSach;
    private Float totalCong;
    private Float lateMinutes;
}

package com.datn.datn_vanh.Dto.Employee;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDto {
    private Long id;
    private String name;
    private String dob;
    private String created_at;
    private String avatar;
    private String email;
    private String phone;
    private String salary_level;
    private Boolean isActivated;
}
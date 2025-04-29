package com.datn.datn_vanh.Controller.Employee;

import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import com.datn.datn_vanh.Entity.Employee;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public interface EmployeeController {


    @GetMapping("/getAll")
    public List<EmployeeDto> getAllEmployee();
}

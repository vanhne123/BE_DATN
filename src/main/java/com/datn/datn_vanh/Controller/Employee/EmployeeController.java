package com.datn.datn_vanh.Controller.Employee;

import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface EmployeeController {


    @GetMapping("/getAll")
    public List<EmployeeDto> getAllEmployee();

    @GetMapping("/getEmployeeById")
    public EmployeeDto getEmployeeById(@RequestParam String employeeId);

    @PutMapping("/updateEmployeeById")
    public void updateEmployeeById(@RequestBody EmployeeDto body);

    @DeleteMapping("/deleteEmployeeById")
    public void deleteEmployeeById(@RequestParam String employeeId);
}

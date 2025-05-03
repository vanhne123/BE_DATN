package com.datn.datn_vanh.Controller.Employee;

import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import com.datn.datn_vanh.Security.JwtUtil;
import com.datn.datn_vanh.Service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;



@RestController
@RequestMapping("/employee")
public class EmployeeControllerImp implements EmployeeController{

    private final EmployeeService employeeService ;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public EmployeeControllerImp(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @Override
    public List<EmployeeDto> getAllEmployee() {
        CompletableFuture<Object> future = employeeService.getAllEmployees();
        Object rawData = future.join();
        List<EmployeeDto> resultList = new ArrayList<>();

        // Kiểm tra kiểu dữ liệu của rawData trước khi ép kiểu
        if (rawData instanceof Map<?, ?>) {
            Map<String, Map<String, Object>> rootMap = (Map<String, Map<String, Object>>) rawData;
            ObjectMapper objectMapper = new ObjectMapper();

            for (Map.Entry<String, Map<String, Object>> entry : rootMap.entrySet()) {
                String employeeId = entry.getKey();  // ID của nhân viên
                Map<String, Object> dataEntry = entry.getValue(); // Dữ liệu nhân viên (là Map)

                if (dataEntry instanceof Map) {
                    try {
                        // Chuyển Map thành EmployeeDto
                        EmployeeDto employee = objectMapper.convertValue(dataEntry, EmployeeDto.class);
                        if (Boolean.TRUE.equals(employee.getIsActivated())) {
                            employee.setId(Long.parseLong(employeeId));
                            resultList.add(employee);
                        }
                    } catch (Exception e) {
                        logger.error("Lỗi khi chuyển đổi dữ liệu nhân viên với ID: {}", employeeId, e);
                    }
                } else {
                    logger.warn("Dữ liệu nhân viên với ID {} không phải là Map hợp lệ.", employeeId);
                }
            }
        } else {
            logger.error("Dữ liệu nhận được không phải là kiểu Map hợp lệ.");
        }

        return resultList;


    }

    @Override
    public EmployeeDto getEmployeeById(String employeeId) {
        if (employeeId == null) {
            logger.warn("employeeId null, không thể truy xuất nhân viên.");
            return null;
        }

        CompletableFuture<Object> future = employeeService.getEmployeeById(employeeId);
        Object rawData = future.join();

        if (rawData == null) {
            logger.warn("Không tìm thấy nhân viên với ID: {}", employeeId);
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        EmployeeDto employeeDto = objectMapper.convertValue(rawData, EmployeeDto.class);
        employeeDto.setId(Long.parseLong(employeeId));
        return employeeDto;
    }

    @Override
    public void updateEmployeeById(EmployeeDto body) {
        employeeService.updateEmployee(body);
        logger.info("Update employee successfully");
    }

    @Override
    public void deleteEmployeeById(String employeeId) {
        employeeService.deleteEmployee(Long.valueOf(employeeId));
        logger.info("Delete employee successfully");
    }

}

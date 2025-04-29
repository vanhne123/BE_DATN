package com.datn.datn_vanh.Service;

import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import com.datn.datn_vanh.ENUM.Reference;
import com.datn.datn_vanh.Security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final Firestore db = FirestoreClient.getFirestore();


    public CompletableFuture<Object> getAllEmployees() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Reference.EMPLOYEE_PATH);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lưu dữ liệu vào CompletableFuture
                    future.complete(dataSnapshot.getValue());
                    logger.info("Lấy dữ liệu thành công từ Firebase path: /{}", Reference.EMPLOYEE_PATH);
                } else {
                    // Trả về null nếu không có dữ liệu
                    future.complete(null);
                    logger.warn("Không tìm thấy dữ liệu tại Firebase path: /{}", Reference.EMPLOYEE_PATH);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
                future.completeExceptionally(new Exception("Lỗi khi lấy dữ liệu từ Firebase: " + databaseError.getMessage()));
                logger.error("Lỗi khi lấy dữ liệu từ Firebase: {}", databaseError.getMessage());
            }
        });

        return future;
    }


    public CompletableFuture<EmployeeDto> getEmployeeById(String employeeId) {
        CompletableFuture<EmployeeDto> future = new CompletableFuture<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("employees");

        reference.child(employeeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    EmployeeDto employee = dataSnapshot.getValue(EmployeeDto.class);
                    if (employee != null) {
                        employee.setId(Long.parseLong(employeeId)); // Set ID vì Firebase có thể không lưu ID bên trong
                        future.complete(employee); // Hoàn thành future với dữ liệu nhân viên
                    } else {
                        future.completeExceptionally(new Exception("Không chuyển đổi được dữ liệu nhân viên."));
                    }
                } else {
                    future.completeExceptionally(new Exception("Không tìm thấy nhân viên với ID: " + employeeId));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new Exception("Lỗi khi truy vấn Firebase: " + databaseError.getMessage()));
            }
        });

        return future;
    }
//try {
//    EmployeeDto employee = getEmployeeById("401862").get(); // .get() sẽ đợi future hoàn thành
//    System.out.println("Nhân viên tìm được: " + employee);
//} catch (Exception e) {
//    System.err.println("Không tìm thấy nhân viên: " + e.getMessage());
//}



}

package com.datn.datn_vanh.Service;

import com.datn.datn_vanh.Dto.Employee.EmployeeDto;
import com.datn.datn_vanh.ENUM.Reference;
import com.datn.datn_vanh.Security.JwtUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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


    public CompletableFuture<Object> getEmployeeById(String employeeId) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        DatabaseReference reference = FirebaseDatabase
                .getInstance()
                .getReference(Reference.EMPLOYEE_PATH)
                .child(employeeId); // Lấy node cụ thể

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.complete(dataSnapshot.getValue());
                    logger.info("Lấy dữ liệu thành công cho employeeId: {}", employeeId);
                } else {
                    future.complete(null);
                    logger.warn("Không tìm thấy employeeId: {}", employeeId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(
                        new Exception("Lỗi khi lấy dữ liệu từ Firebase: " + databaseError.getMessage())
                );
                logger.error("Lỗi Firebase với employeeId {}: {}", employeeId, databaseError.getMessage());
            }
        });

        return future;
    }

    public void updateEmployee(EmployeeDto body) {
        if (body.getId() == null) {
            logger.warn("employeeId null, không thể cập nhật.");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("employees")
                .child(String.valueOf(body.getId()));

        Map<String, Object> updates = new HashMap<>();
        if (body.getDob() != null) {
            updates.put("dob", body.getDob());
        }
        if (body.getName() != null) {
            updates.put("name", body.getName());
        }
        if (body.getEmail() != null) {
            updates.put("email", body.getEmail());
        }
        if (body.getPhone() != null) {
            updates.put("phone", body.getPhone());
        }
        if (body.getSalary_level() != null) {
            updates.put("salary_level", body.getSalary_level());
        }

        if (updates.isEmpty()) {
            logger.info("Không có trường nào cần cập nhật cho employeeId: {}", body.getId());
            return;
        }

        ApiFuture<Void> future = ref.updateChildrenAsync(updates);

        future.addListener(() -> {
            try {
                future.get(); // Nếu có exception, nó sẽ được ném ở đây
                logger.info("Đã cập nhật dob và name cho employeeId: {}", body.getId());
            } catch (Exception e) {
                logger.error("Lỗi khi cập nhật employeeId {}: {}", body.getId(), e.getMessage(), e);
            }
        }, Executors.newSingleThreadExecutor());

    }

    /*
    soft delete
     */
    public void deleteEmployee(Long employeeId) {
        if (employeeId == null) {
            logger.warn("employeeId null, không thể cập nhật.");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("employees")
                .child(String.valueOf(employeeId));

        Map<String, Object> updates = new HashMap<>();
        updates.put("isActivated", false);

        ApiFuture<Void> future = ref.updateChildrenAsync(updates);

        future.addListener(() -> {
            try {
                future.get(); // Nếu có exception, nó sẽ được ném ở đây
                logger.info("Đã xóa cho employeeId: {}", employeeId);
            } catch (Exception e) {
                logger.error("Lỗi khi xóa employeeId {}: {}", employeeId, e.getMessage(), e);
            }
        }, Executors.newSingleThreadExecutor());

    }




}

package com.datn.datn_vanh.Service;

import com.datn.datn_vanh.ENUM.Reference;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class RecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(RecognitionService.class);

    /**
     * Lấy tất cả dữ liệu từ nút /recognitions trong Firebase Realtime Database.
     * @return CompletableFuture chứa dữ liệu (thường là Map<String, Object> hoặc List<Object>),
     *         hoặc null nếu không có dữ liệu hoặc có lỗi.
     */
    public CompletableFuture<Object> getAllRecognitions() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Reference.RECOGNITIONS_PATH);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lưu dữ liệu vào CompletableFuture
                    future.complete(dataSnapshot.getValue());
                    logger.info("Lấy dữ liệu thành công từ Firebase path: /{}", Reference.RECOGNITIONS_PATH);
                } else {
                    // Trả về null nếu không có dữ liệu
                    future.complete(null);
                    logger.warn("Không tìm thấy dữ liệu tại Firebase path: /{}", Reference.RECOGNITIONS_PATH);
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

    public CompletableFuture<Object> getEmployeeRecogniById(String employeeId) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        DatabaseReference reference = FirebaseDatabase
                .getInstance()
                .getReference(Reference.RECOGNITIONS_PATH)
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

}

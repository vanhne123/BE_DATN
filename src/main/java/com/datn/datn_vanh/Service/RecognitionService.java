package com.datn.datn_vanh.Service;

import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(RecognitionService.class);
    private static final String RECOGNITIONS_PATH = "recognitions"; // Đường dẫn tới nút cần lấy

    /**
     * Lấy tất cả dữ liệu từ nút /recognitions trong Firebase Realtime Database.
     * @return CompletableFuture chứa dữ liệu (thường là Map<String, Object> hoặc List<Object>),
     *         hoặc null nếu không có dữ liệu hoặc có lỗi.
     */
    public CompletableFuture<Object> getAllRecognitions() {
        CompletableFuture<Object> future = new CompletableFuture<>();

        // Lấy instance của FirebaseDatabase (đã được khởi tạo bởi FireBaseConfig)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(RECOGNITIONS_PATH);

        // Sử dụng addListenerForSingleValueEvent để lấy dữ liệu bất đồng bộ
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lưu dữ liệu vào CompletableFuture
                    future.complete(dataSnapshot.getValue());
                    logger.info("Lấy dữ liệu thành công từ Firebase path: /{}", RECOGNITIONS_PATH);
                } else {
                    // Trả về null nếu không có dữ liệu
                    future.complete(null);
                    logger.warn("Không tìm thấy dữ liệu tại Firebase path: /{}", RECOGNITIONS_PATH);
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
}

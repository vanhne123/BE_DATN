# --- Stage 1: Extract JAR layers ---
FROM eclipse-temurin:17-jre as builder

WORKDIR /layers
COPY target/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract
# Gỡ nén các lớp của JAR để tối ưu cache

# --- Stage 2: Final image ---
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy từng layer riêng biệt
COPY --from=builder /layers/dependencies/ .
COPY --from=builder /layers/snapshot-dependencies/ .
COPY --from=builder /layers/spring-boot-loader/ .
COPY --from=builder /layers/application/ .

# Copy file cấu hình cần thiết
COPY target/classes/jsonTemplateLayout.json /app/config/jsonTemplateLayout.json

# Set timezone nếu cần
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Mở port 2608
EXPOSE 2608

# Chạy bằng JarLauncher để boot từ layer
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17-focal AS builder
WORKDIR /app
COPY pom.xml .
# Tải dependencies trước để cache (tiết kiệm thời gian build lại sau này)
RUN mvn dependency:go-offline -B
COPY src ./src
# Build ứng dụng, bỏ qua test
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng bằng JRE gọn nhẹ
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
# Copy file jar từ builder sang
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

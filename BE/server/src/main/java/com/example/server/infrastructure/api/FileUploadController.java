package com.example.server.infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    private final Path rootLocation = Paths.get("uploads");

    /** Chỉ cho phép tệp ảnh và video (tránh upload .html/.exe... bị serve tĩnh gây XSS/lưu mã độc). */
    private static final java.util.Set<String> DUOI_CHO_PHEP = java.util.Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".avif",
            ".mp4", ".webm", ".ogg", ".mov", ".m4v", ".avi", ".mkv");

    public FileUploadController() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @PostMapping
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to store empty file."));
            }
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                String contentType = file.getContentType();
                if (contentType != null) {
                    if (contentType.equals("image/jpeg")) extension = ".jpg";
                    else if (contentType.equals("image/png")) extension = ".png";
                    else if (contentType.equals("image/gif")) extension = ".gif";
                    else if (contentType.equals("image/webp")) extension = ".webp";
                    else extension = ".jpg";
                } else {
                    extension = ".jpg";
                }
            }
            if (!DUOI_CHO_PHEP.contains(extension.toLowerCase())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ chấp nhận tệp ảnh hoặc video."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path destinationFile = rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Cannot store file outside current directory."));
            }

            file.transferTo(destinationFile);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(newFilename)
                    .toUriString();

            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", Map.of("url", fileDownloadUri)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Could not upload the file: " + e.getMessage()));
        }
    }
}

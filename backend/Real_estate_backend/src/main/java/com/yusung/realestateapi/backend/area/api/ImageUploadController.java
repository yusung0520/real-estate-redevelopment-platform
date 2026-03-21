package com.yusung.realestateapi.backend.area.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/images")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "업로드할 파일이 없습니다."
                ));
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";

            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex != -1) {
                ext = originalFilename.substring(dotIndex);
            }

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String savedFileName = UUID.randomUUID() + ext;
            File dest = new File(dir, savedFileName);
            file.transferTo(dest);

            String imageUrl = "/uploads/" + savedFileName;

            return ResponseEntity.ok(Map.of(
                    "url", imageUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "이미지 저장 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}
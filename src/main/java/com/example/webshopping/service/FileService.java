package com.example.webshopping.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return null;
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);

            }
            //파일명 생성 (UUID + 원본파일명)
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "-" + originalFilename;

            //파일저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/images/product/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }

    }

    public String uploadCategoryImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return null;
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            //파일명 생성 (UUID + 원본파일명)
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "-" + originalFilename;

            //파일저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/images/product/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("카테고리 이미지 저장 실패: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/images/product")) {
                String fileName = fileUrl.substring("/images/product/".length());
                Path filePath = Paths.get(uploadDir).resolve(fileName);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패 : " + e.getMessage());
        }

    }
}

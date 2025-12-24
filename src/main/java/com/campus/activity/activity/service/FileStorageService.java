package com.campus.activity.activity.service;

import com.campus.activity.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public String store(MultipartFile file) {
        validate(file);
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            String ext = extension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path dest = dir.resolve(filename);
            file.transferTo(dest);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new BizException(44001, "file upload failed");
        }
    }

    public String storeInSubdir(MultipartFile file, String subDir) {
        validate(file);
        String safeSubdir = subDir == null ? "" : subDir.trim();
        if (safeSubdir.contains("..")) {
            throw new BizException(44001, "invalid path");
        }
        try {
            Path dir = safeSubdir.isEmpty() ? Paths.get(uploadDir) : Paths.get(uploadDir, safeSubdir);
            Files.createDirectories(dir);
            String ext = extension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path dest = dir.resolve(filename);
            file.transferTo(dest);
            String prefix = safeSubdir.isEmpty() ? "/uploads/" : "/uploads/" + safeSubdir + "/";
            return prefix + filename;
        } catch (IOException e) {
            throw new BizException(44001, "file upload failed");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(44001, "file upload failed");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new BizException(44001, "invalid file type");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BizException(44001, "file too large");
        }
    }

    private String extension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i);
    }
}

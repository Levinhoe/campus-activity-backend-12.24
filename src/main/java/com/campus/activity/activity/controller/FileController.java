package com.campus.activity.activity.controller;

import com.campus.activity.activity.service.FileStorageService;
import com.campus.activity.activity.service.UserService;
import com.campus.activity.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import com.campus.activity.security.SecurityUtil;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    public FileController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @PostMapping("/avatar")
    @Operation(summary = "Upload avatar")
    public ApiResult<Map<String, String>> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String url = fileStorageService.storeInSubdir(file, "avatar");
        Long userId = SecurityUtil.getUserId();
        userService.updateAvatar(userId, url);
        return ApiResult.ok(Map.of("url", url));
    }

    @PostMapping("/poster")
    @Operation(summary = "Upload poster")
    public ApiResult<String> uploadPoster(@RequestPart("file") MultipartFile file) {
        String url = fileStorageService.store(file);
        return ApiResult.ok(url);
    }
}

package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.response.NotificationVO;
import com.campus.activity.activity.entity.Notification;
import com.campus.activity.activity.repository.NotificationRepository;
import com.campus.activity.common.PageResult;
import com.campus.activity.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public void notify(Long userId, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(n);
    }

    public void notifyRegistration(Long userId, Long activityId, String activityTitle, boolean approved, String reason) {
        String payload = buildRegistrationPayload(activityId, activityTitle, approved, reason);
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(approved ? "Registration approved" : "Registration rejected");
        n.setContent(payload);
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(n);
    }

    public void notifyFeedback(Long userId, Long activityId, Integer ratingScore, String suggestion) {
        String payload = buildFeedbackPayload(activityId, ratingScore, suggestion);
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle("Survey feedback");
        n.setContent(payload);
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(n);
    }

    public PageResult<NotificationVO> list(Long userId, int page, int size) {
        Page<Notification> pageData =
                notificationRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size));
        List<NotificationVO> records = pageData.stream()
                .map(n -> {
                    ParsedContent parsed = parseContent(n.getContent());
                    String content = parsed.content == null ? n.getContent() : parsed.content;
                    return new NotificationVO(
                            n.getId(),
                            n.getTitle(),
                            content,
                            parsed.activityId,
                            parsed.activityTitle,
                            parsed.activityName,
                            parsed.name,
                            parsed.ratingScore,
                            parsed.suggestion,
                            parsed.type,
                            Boolean.TRUE.equals(n.getReadFlag()),
                            n.getCreatedAt() == null ? null : n.getCreatedAt().toString()
                    );
                })
                .toList();
        return PageResult.of(records, pageData.getTotalElements(), page, size);
    }

    public void markRead(Long userId, Long id) {
        Notification n = notificationRepo.findById(id)
                .orElseThrow(() -> new BizException(43001, "notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new BizException(40002, "forbidden");
        }
        n.setReadFlag(true);
        notificationRepo.save(n);
    }

    public void delete(Long userId, Long id) {
        Notification n = notificationRepo.findById(id)
                .orElseThrow(() -> new BizException(43001, "notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new BizException(40002, "forbidden");
        }
        notificationRepo.delete(n);
    }

    private String buildFeedbackPayload(Long activityId, Integer ratingScore, String suggestion) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FEEDBACK");
        payload.put("content", "Student submitted feedback");
        payload.put("activityId", activityId);
        payload.put("ratingScore", ratingScore);
        payload.put("suggestion", suggestion);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "Student submitted feedback";
        }
    }

    private String buildRegistrationPayload(Long activityId, String activityTitle, boolean approved, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "REGISTRATION");
        payload.put("content", approved ? "Registration approved" : "Registration rejected");
        payload.put("activityId", activityId);
        payload.put("activityTitle", activityTitle);
        payload.put("activityName", activityTitle);
        payload.put("name", activityTitle);
        if (!approved && reason != null) {
            payload.put("reason", reason);
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return approved ? "Registration approved" : "Registration rejected";
        }
    }

    private ParsedContent parseContent(String content) {
        if (content == null || !content.trim().startsWith("{")) {
            return new ParsedContent(null, null, null, null, null, null, null, null);
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(content, Map.class);
            String type = map.get("type") == null ? null : map.get("type").toString();
            String text = map.get("content") == null ? null : map.get("content").toString();
            Long activityId = toLong(map.get("activityId"));
            String activityTitle = map.get("activityTitle") == null ? null : map.get("activityTitle").toString();
            String activityName = map.get("activityName") == null ? null : map.get("activityName").toString();
            String name = map.get("name") == null ? null : map.get("name").toString();
            Integer ratingScore = toInt(map.get("ratingScore"));
            String suggestion = map.get("suggestion") == null ? null : map.get("suggestion").toString();
            return new ParsedContent(text, activityId, activityTitle, activityName, name, ratingScore, suggestion, type);
        } catch (Exception e) {
            return new ParsedContent(null, null, null, null, null, null, null, null);
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record ParsedContent(
            String content,
            Long activityId,
            String activityTitle,
            String activityName,
            String name,
            Integer ratingScore,
            String suggestion,
            String type
    ) {}
}

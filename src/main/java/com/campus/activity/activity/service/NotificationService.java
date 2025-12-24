package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.response.NotificationVO;
import com.campus.activity.activity.entity.Notification;
import com.campus.activity.activity.repository.NotificationRepository;
import com.campus.activity.common.PageResult;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public void notify(Long userId, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(n);
    }

    public PageResult<NotificationVO> list(Long userId, int page, int size) {
        Page<Notification> pageData =
                notificationRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size));
        List<NotificationVO> records = pageData.stream()
                .map(n -> new NotificationVO(
                        n.getId(),
                        n.getTitle(),
                        n.getContent(),
                        Boolean.TRUE.equals(n.getReadFlag()),
                        n.getCreatedAt() == null ? null : n.getCreatedAt().toString()
                ))
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
}

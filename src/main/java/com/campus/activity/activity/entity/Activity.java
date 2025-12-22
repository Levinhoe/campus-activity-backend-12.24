package com.campus.activity.activity.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 50)
    private String category;

    @Column(length = 100)
    private String location;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "cover_url", length = 255)
    private String coverUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "signup_start", nullable = false)
    private LocalDateTime signupStart;

    @Column(name = "signup_end", nullable = false)
    private LocalDateTime signupEnd;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "signed_count", nullable = false)
    private Integer signedCount;

    @Column(nullable = false)
    private Byte status; // 0/1/2/3 对应 ActivityStatus

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}

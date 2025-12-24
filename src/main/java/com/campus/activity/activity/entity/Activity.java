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

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "signup_start")
    private LocalDateTime enrollStart;

    @Column(name = "signup_end")
    private LocalDateTime enrollDeadline;

    @Column(nullable = false)
    private Byte status; // domain-specific status if any

    @Column(nullable = true)
    private Integer capacity;

    @Column(name = "signed_count", nullable = false)
    private Integer enrolledCount;

    @Column(name = "is_volunteer", nullable = false)
    private Boolean isVolunteer;

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

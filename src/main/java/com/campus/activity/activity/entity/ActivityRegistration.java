package com.campus.activity.activity.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "activity_registration",
        uniqueConstraints = @UniqueConstraint(name = "uk_activity_user", columnNames = {"activity_id","user_id"})
)
public class ActivityRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reg_id")
    private Long regId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Byte status; // 1已报名 2已取消

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}

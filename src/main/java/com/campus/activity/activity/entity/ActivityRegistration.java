package com.campus.activity.activity.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "activity_registration",
        uniqueConstraints = @UniqueConstraint(name = "uk_activity_user", columnNames = {"activity_id","user_id"}),
        indexes = {
                @Index(name = "idx_activity", columnList = "activity_id"),
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_student_no", columnList = "student_no")
        }
)
public class ActivityRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reg_id")
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "student_no", nullable = false, length = 30)
    private String studentNo;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private Byte status; // 0 pending, 1 approved, 2 rejected, 3 canceled

    @Column(name = "audit_reason", length = 200)
    private String auditReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

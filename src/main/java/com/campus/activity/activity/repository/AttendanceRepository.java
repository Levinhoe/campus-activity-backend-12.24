package com.campus.activity.activity.repository;

import com.campus.activity.activity.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByActivityIdAndUserId(Long activityId, Long userId);
    Optional<Attendance> findByActivityIdAndStudentNo(Long activityId, String studentNo);
    List<Attendance> findByActivityId(Long activityId);
    void deleteByActivityId(Long activityId);
}

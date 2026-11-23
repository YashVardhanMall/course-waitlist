package com.example.coursewaitlist.model;

import java.time.LocalDateTime;

public class WaitlistEntry {

    private final Long courseId;
    private final Long studentId;
    private final LocalDateTime joinedAt;

    public WaitlistEntry(Long courseId, Long studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

}

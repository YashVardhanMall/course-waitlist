package com.example.coursewaitlist.model;

import java.time.LocalDateTime;

public class Enrollment {

	private final Long courseId;
    private final Long studentId;
    private final LocalDateTime enrolledAt;
	
    public Enrollment(Long courseId, Long studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.enrolledAt = LocalDateTime.now();
    }
    
	public Long getCourseId() {
		return courseId;
	}
	public Long getStudentId() {
		return studentId;
	}
	public LocalDateTime getEnrolledAt() {
		return enrolledAt;
	}
    
    
    
}

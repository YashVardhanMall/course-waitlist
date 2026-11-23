package com.example.coursewaitlist.dto;

import com.example.coursewaitlist.model.EnrollmentStatus;

public class EnrollmentResponse {
	
	private final EnrollmentStatus status;
    private final String message;
    private final Long promotedStudentId;

    public EnrollmentResponse(
            EnrollmentStatus status,
            String message,
            Long promotedStudentId) {

        this.status = status;
        this.message = message;
        this.promotedStudentId = promotedStudentId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Long getPromotedStudentId() {
        return promotedStudentId;
    }
}

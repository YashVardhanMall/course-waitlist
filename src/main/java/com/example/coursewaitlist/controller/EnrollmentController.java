package com.example.coursewaitlist.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.coursewaitlist.dto.EnrollmentRequest;
import com.example.coursewaitlist.dto.EnrollmentResponse;
import com.example.coursewaitlist.model.Enrollment;
import com.example.coursewaitlist.model.EnrollmentStatus;
import com.example.coursewaitlist.model.WaitlistEntry;
import com.example.coursewaitlist.service.EnrollmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/courses/{courseId}")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollmentRequest request) {

        EnrollmentResponse response = enrollmentService.enroll(
                courseId, request.getStudentId()
        );

        return ResponseEntity
                .status(getHttpStatus(response.getStatus()))
                .body(response);
    }

    @DeleteMapping("/enrollments/{studentId}")
    public ResponseEntity<EnrollmentResponse> drop(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {

        EnrollmentResponse response =
                enrollmentService.drop(courseId, studentId);

        return ResponseEntity
                .status(getHttpStatus(response.getStatus()))
                .body(response);
    }

    @GetMapping("/enrollments")
    public ResponseEntity<List<Enrollment>> getEnrollments(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                enrollmentService.getEnrollments(courseId)
        );
    }

    @GetMapping("/waitlist")
    public ResponseEntity<List<WaitlistEntry>> getWaitlist(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                enrollmentService.getWaitlist(courseId)
        );
    }

    private HttpStatus getHttpStatus(EnrollmentStatus status) {

        return switch (status) {

            case ENROLLED -> HttpStatus.CREATED;

            case WAITLISTED, DROPPED -> HttpStatus.OK;

            case ALREADY_ENROLLED,
                 ALREADY_WAITLISTED,
                 NOT_ENROLLED -> HttpStatus.CONFLICT;

            case PREREQUISITE_NOT_MET ->
                    HttpStatus.UNPROCESSABLE_ENTITY;

            case COURSE_NOT_FOUND,
                 STUDENT_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
        };
    }
}

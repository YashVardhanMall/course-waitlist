package com.example.coursewaitlist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.coursewaitlist.dto.EnrollmentResponse;
import com.example.coursewaitlist.model.Course;
import com.example.coursewaitlist.model.Enrollment;
import com.example.coursewaitlist.model.EnrollmentStatus;
import com.example.coursewaitlist.model.Student;
import com.example.coursewaitlist.model.WaitlistEntry;
import com.example.coursewaitlist.repository.InMemoryRepo;

@Service
public class EnrollmentService {
	
	private final InMemoryRepo repository;
	
	public EnrollmentService(InMemoryRepo repository) {
        this.repository = repository;
    }
	
	//Enrollment
	
	public synchronized EnrollmentResponse enroll(
            Long courseId, Long studentId) {

	    // 1. Validate IDs
	    if (courseId == null || studentId == null
	            || courseId <= 0 || studentId <= 0) {
	        return response(
	            EnrollmentStatus.INVALID_REQUEST,
	            "Course ID and Student ID must be positive."
	        );
	    }

	    // 2. Validate course
	    Optional<Course> courseOptional =
	            repository.findCourseById(courseId);

	    if (courseOptional.isEmpty()) {
	        return response(
	            EnrollmentStatus.COURSE_NOT_FOUND,
	            "Course not found"
	        );
	    }

	    Course course = courseOptional.get();

	    // 3. Validate student
	    Optional<Student> studentOptional =
	            repository.findStudentById(studentId);

	    if (studentOptional.isEmpty()) {
	        return response(
	            EnrollmentStatus.STUDENT_NOT_FOUND,
	            "Student not found"
	        );
	    }

	    Student student = studentOptional.get();

	    // 4. Prevent duplicate enrollment
	    if (repository.isEnrolled(courseId, studentId)) {
	        return response(
	            EnrollmentStatus.ALREADY_ENROLLED,
	            "Student is already enrolled."
	        );
	    }

	    // 5. Prevent duplicate waitlist entry
	    if (repository.isWaitlisted(courseId, studentId)) {
	        return response(
	            EnrollmentStatus.ALREADY_WAITLISTED,
	            "Student is already on the waitlist."
	        );
	    }

	    // 6. Check prerequisite
	    if (!student.isPrerequisiteCompleted()) {
	        return response(
	            EnrollmentStatus.PREREQUISITE_NOT_MET,
	            "Student has not completed the prerequisite."
	        );
	    }

	    // 7. Check capacity
	    if (repository.getEnrollmentCount(courseId)
	            < course.getCapacity()) {

	        boolean added = repository.addEnrollment(
	            new Enrollment(courseId, studentId)
	        );

	        if (!added) {
	            return response(
	                EnrollmentStatus.ALREADY_ENROLLED,
	                "Student is already enrolled."
	            );
	        }

	        return response(
	            EnrollmentStatus.ENROLLED,
	            "Student enrolled successfully."
	        );
	    }

	    // 8. Add to FIFO waitlist
	    boolean added = repository.addToWaitlist(
	        new WaitlistEntry(courseId, studentId)
	    );

	    if (!added) {
	        return response(
	            EnrollmentStatus.ALREADY_WAITLISTED,
	            "Student is already on the waitlist."
	        );
	    }

	    return response(
	        EnrollmentStatus.WAITLISTED,
	        "Student added to waitlist."
	    );
	    }
	
	//drop enrollment
	public synchronized EnrollmentResponse drop(
            Long courseId, Long studentId) {

        if (courseId == null || studentId == null || courseId <= 0 || studentId <= 0) {
            return response(EnrollmentStatus.INVALID_REQUEST,"Course ID and Student ID are required.");
        }

        Course course = repository.findCourseById(courseId).orElse(null);

        if (course == null) {
            return response(EnrollmentStatus.COURSE_NOT_FOUND,"Course does not exist.");
        }

        Student student = repository.findStudentById(studentId).orElse(null);

        if (student == null) {
            return response(EnrollmentStatus.STUDENT_NOT_FOUND,"Student does not exist.");
        }

        // Remove enrollment exactly once
        boolean removed = repository.removeEnrollment(courseId, studentId);

        if (!removed) {
            return response(EnrollmentStatus.NOT_ENROLLED,"Student is not currently enrolled.");
        }

        // A seat is now available. Attempt FIFO promotion.
        Long promotedStudentId = promoteNextEligibleStudent(courseId);

        if (promotedStudentId != null) {
            return new EnrollmentResponse(EnrollmentStatus.DROPPED,"Student dropped successfully. Waitlisted student promoted.",promotedStudentId);
        }

        return response(EnrollmentStatus.DROPPED, "Student dropped successfully. No eligible waitlisted student.");
    }
	
    // FIFO WAITLIST PROMOTION
    private Long promoteNextEligibleStudent(Long courseId) {

        while (repository.getEnrollmentCount(courseId) < repository.findCourseById(courseId).orElseThrow().getCapacity()) {

            // Get and remove the earliest waitlist entry
            WaitlistEntry entry = repository.pollWaitlist(courseId).orElse(null);

            if (entry == null) {
                return null;
            }

            Long studentId = entry.getStudentId();

            Student student = repository.findStudentById(studentId).orElse(null);

            // Skip invalid or ineligible entries
            if (student == null || !student.isPrerequisiteCompleted() || repository.isEnrolled(courseId, studentId)) {

                continue;
            }

            // Promote the earliest eligible student
            boolean added = repository.addEnrollment(new Enrollment(courseId, studentId));

            if (added) {
                return studentId;
            }
        }

        return null;
    }

    // QUERY METHODS

    public synchronized List<Enrollment> getEnrollments(Long courseId) {

        if (courseId == null ||
                repository.findCourseById(courseId).isEmpty()) {
            throw new IllegalArgumentException("Invalid course ID.");
        }

        return repository.getEnrollments(courseId);
    }

    public synchronized List<WaitlistEntry> getWaitlist(Long courseId) {

        if (courseId == null ||
                repository.findCourseById(courseId).isEmpty()) {
            throw new IllegalArgumentException("Invalid course ID.");
        }

        return repository.getWaitlist(courseId);
    }

    // RESPONSE HELPER

    private EnrollmentResponse response(
            EnrollmentStatus status, String message) {

        return new EnrollmentResponse(status, message, null);
    }
	
        
    
}

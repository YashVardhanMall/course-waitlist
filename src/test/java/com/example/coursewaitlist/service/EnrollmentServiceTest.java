package com.example.coursewaitlist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.coursewaitlist.model.Course;
import com.example.coursewaitlist.model.EnrollmentStatus;
import com.example.coursewaitlist.model.Student;
import com.example.coursewaitlist.repository.InMemoryRepo;

public class EnrollmentServiceTest {

	private InMemoryRepo repo;
	private EnrollmentService service;
	
	@BeforeEach
	void setUp() {
		repo = new InMemoryRepo();
		service = new EnrollmentService(repo);
		
		repo.saveCourse(new Course(1L,"Java Advanced Prgramming",2));
		
		repo.saveStudent(new Student(101L,"Rahul",true));
		repo.saveStudent(new Student(102L,"Aman",true));
		repo.saveStudent(new Student(103L,"Tanq",true));
		repo.saveStudent(new Student(104L,"Yash",true));
		repo.saveStudent(new Student(105L,"Ash",false));
		
	}
	
	@Test
	void shouldEnrollStudentWhenSeatIsAvailable() {
		var response = service.enroll(1L, 101L);
		
		assertEquals(EnrollmentStatus.ENROLLED, response.getStatus());
		assertEquals(1,repo.getEnrollments(1L).size());
	}
	
	@Test
	void shouldWaitlistStudentWhenCouseIsFull() {
		service.enroll(1L, 101L);
		service.enroll(1L, 102L);
		
		var resp = service.enroll(1L, 103L);
		
		assertEquals(EnrollmentStatus.WAITLISTED,resp.getStatus());
		assertEquals(2,repo.getEnrollmentCount(1L));
		assertEquals(1,repo.getWaitlistCount(1L));
	}
	
	@Test
    void shouldPreventDuplicateEnrollment() {
        service.enroll(1L, 101L);

        var response = service.enroll(1L, 101L);

        assertEquals(EnrollmentStatus.ALREADY_ENROLLED, response.getStatus());
        assertEquals(1, repo.getEnrollmentCount(1L));
    }

    @Test
    void shouldRejectStudentWithoutPrerequisite() {
        var response = service.enroll(1L, 105L);

        assertEquals(
            EnrollmentStatus.PREREQUISITE_NOT_MET,
            response.getStatus()
        );

        assertEquals(0, repo.getEnrollmentCount(1L));
        assertEquals(0, repo.getWaitlistCount(1L));
    }
	
    @Test
    void shouldPromoteFirstWaitlistedStudentAfterDrop() {
        // Fill course capacity
        service.enroll(1L, 101L);
        service.enroll(1L, 102L);

        // Add two students to waitlist
        service.enroll(1L, 103L);
        repo.saveStudent(new Student(104L, "Neha", true));
        service.enroll(1L, 104L);

        // First enrolled student drops
        var response = service.drop(1L, 101L);

        assertEquals(EnrollmentStatus.DROPPED, response.getStatus());
        assertEquals(103L, response.getPromotedStudentId());

        // Verify remaining enrollment order
        var enrollments = repo.getEnrollments(1L);

        assertEquals(2, enrollments.size());
        assertEquals(102L, enrollments.get(0).getStudentId());
        assertEquals(103L, enrollments.get(1).getStudentId());

        // Neha should still be waiting
        assertEquals(1, repo.getWaitlistCount(1L));
    }
	
    @Test
    void shouldSkipIneligibleStudentAndPromoteNextEligibleStudent() {
        service.enroll(1L, 101L);
        service.enroll(1L, 102L);

        // Karan is ineligible; Priya is eligible
        service.enroll(1L, 105L);
        service.enroll(1L, 103L);

        var response = service.drop(1L, 101L);

        assertEquals(EnrollmentStatus.DROPPED, response.getStatus());
        assertEquals(103L, response.getPromotedStudentId());

        assertEquals(0, repo.getWaitlistCount(1L));
        assertEquals(2, repo.getEnrollmentCount(1L));
    }
    
    @Test
    void shouldNotAllowStudentToDropTwice() {
        service.enroll(1L, 101L);

        var firstResponse = service.drop(1L, 101L);
        var secondResponse = service.drop(1L, 101L);

        assertEquals(EnrollmentStatus.DROPPED, firstResponse.getStatus());
        assertEquals(EnrollmentStatus.NOT_ENROLLED, secondResponse.getStatus());
        assertEquals(0, repo.getEnrollmentCount(1L));
    }
    
    @Test
    void shouldPreventDuplicateWaitlistEntry() {
        service.enroll(1L, 101L);
        service.enroll(1L, 102L);

        service.enroll(1L, 103L);
        var response = service.enroll(1L, 103L);

        assertEquals(EnrollmentStatus.ALREADY_WAITLISTED, response.getStatus());
        assertEquals(1, repo.getWaitlistCount(1L));
    }
    
    @Test
    void shouldHandleConcurrentEnrollmentsWithoutExceedingCapacity()
            throws Exception {

        int numberOfStudents = 10;

        // Register 10 eligible students
        for (long id = 201; id <= 210; id++) {
            repo.saveStudent(new Student(id, "Student" + id, true));
        }

        CountDownLatch ready = new CountDownLatch(numberOfStudents);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor =
            Executors.newFixedThreadPool(numberOfStudents);

        try {
            List<Future<EnrollmentStatus>> futures = new ArrayList<>();

            for (long id = 201; id <= 210; id++) {
                final long studentId = id;

                Future<EnrollmentStatus> future = executor.submit(() -> {
                    ready.countDown();

                    // Wait until all threads are ready
                    start.await();

                    return service.enroll(1L, studentId).getStatus();
                });

                futures.add(future);
            }

            // Wait until all threads are ready
            ready.await();

            // Release all threads together
            start.countDown();

            int enrolled = 0;
            int waitlisted = 0;

            for (Future<EnrollmentStatus> future : futures) {
                EnrollmentStatus status = future.get();

                if (status == EnrollmentStatus.ENROLLED) {
                    enrolled++;
                } else if (status == EnrollmentStatus.WAITLISTED) {
                    waitlisted++;
                }
            }

            assertEquals(2, enrolled);
            assertEquals(8, waitlisted);

            assertEquals(2, repo.getEnrollmentCount(1L));
            assertEquals(8, repo.getWaitlistCount(1L));

        } finally {
            executor.shutdownNow();
        }
    }
    
    @Test
    void shouldRejectInvalidIds() {
        var zeroIdResponse = service.enroll(1L, 0L);
        var negativeIdResponse = service.enroll(1L, -101L);
        var invalidCourseResponse = service.enroll(-1L, 101L);

        assertEquals(
            EnrollmentStatus.INVALID_REQUEST,
            zeroIdResponse.getStatus()
        );

        assertEquals(
            EnrollmentStatus.INVALID_REQUEST,
            negativeIdResponse.getStatus()
        );

        assertEquals(
            EnrollmentStatus.INVALID_REQUEST,
            invalidCourseResponse.getStatus()
        );

        assertEquals(0, repo.getEnrollmentCount(1L));
        assertEquals(0, repo.getWaitlistCount(1L));
    }
    
    @Test
    void shouldReturnCourseNotFoundForUnknownCourse() {
        var response = service.enroll(999L, 101L);

        assertEquals(
            EnrollmentStatus.COURSE_NOT_FOUND,
            response.getStatus()
        );

        assertEquals(0, repo.getEnrollmentCount(1L));
    }
    
    @Test
    void shouldReturnStudentNotFoundForUnknownStudent() {
        var response = service.enroll(1L, 999L);

        assertEquals(
            EnrollmentStatus.STUDENT_NOT_FOUND,
            response.getStatus()
        );

        assertEquals(0, repo.getEnrollmentCount(1L));
    }
}

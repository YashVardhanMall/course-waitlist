package com.example.coursewaitlist.repository;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.coursewaitlist.model.Course;
import com.example.coursewaitlist.model.Enrollment;
import com.example.coursewaitlist.model.Student;
import com.example.coursewaitlist.model.WaitlistEntry;

@Repository
public class InMemoryRepo {
	
	//store course using courseId as key
	private final Map<Long,Course> courses = new HashMap<>();
	
	//store student using studentId as key
	private final Map<Long, Student> students = new HashMap<>();
	
	//courseId -> studentid -> enrollment
	private final Map<Long, LinkedHashMap<Long,Enrollment>> enrollments = new HashMap<>();
	
	//courseid -> FIFO waitlist
	private final Map<Long, ArrayDeque<WaitlistEntry>> waitlists = new HashMap<>();
	
	//course operation
	public void saveCourse(Course course) {
		courses.put(course.getCourseId(), course);
        enrollments.putIfAbsent(course.getCourseId(), new LinkedHashMap<>());
        waitlists.putIfAbsent(course.getCourseId(), new ArrayDeque<>());
	}
	
	public Optional<Course> findCourseById(Long courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }
	
	//student operation
	public void saveStudent(Student student) {
        students.put(student.getStudentId(), student);
    }

    public Optional<Student> findStudentById(Long studentId) {
        return Optional.ofNullable(students.get(studentId));
    }
    
    //enrollment operation
    public boolean addEnrollment(Enrollment enrollment) {

        Long courseId = enrollment.getCourseId();
        Long studentId = enrollment.getStudentId();

        LinkedHashMap<Long, Enrollment> courseEnrollments =
                enrollments.get(courseId);

        if (courseEnrollments == null) {
            return false;
        }

        // Prevent duplicate enrollment
        return courseEnrollments.putIfAbsent(studentId, enrollment) == null;
    }

    public boolean removeEnrollment(Long courseId, Long studentId) {

        Map<Long, Enrollment> courseEnrollments =
                enrollments.get(courseId);

        if (courseEnrollments == null) {
            return false;
        }

        return courseEnrollments.remove(studentId) != null;
    }

    public boolean isEnrolled(Long courseId, Long studentId) {

        Map<Long, Enrollment> courseEnrollments =
                enrollments.get(courseId);

        return courseEnrollments != null && courseEnrollments.containsKey(studentId);
    }

    public int getEnrollmentCount(Long courseId) {

        Map<Long, Enrollment> courseEnrollments = enrollments.get(courseId);

        return courseEnrollments == null ? 0 : courseEnrollments.size();
    }

    public List<Enrollment> getEnrollments(Long courseId) {

        Map<Long, Enrollment> courseEnrollments =
                enrollments.get(courseId);

        if (courseEnrollments == null) {
            return List.of();
        }

        return List.copyOf(courseEnrollments.values());
    }
    
    //waitlisting operation
    
    public boolean addToWaitlist(WaitlistEntry entry) {

        Long courseId = entry.getCourseId();
        Long studentId = entry.getStudentId();

        ArrayDeque<WaitlistEntry> queue = waitlists.get(courseId);

        if (queue == null) {
            return false;
        }

        // Prevent duplicate waitlist entries
        boolean alreadyPresent = queue.stream()
                .anyMatch(e -> e.getStudentId().equals(studentId));

        if (alreadyPresent) {
            return false;
        }

        // Add at the end of the queue
        return queue.offerLast(entry);
    }

    // Get and remove earliest waitlisted student
    public Optional<WaitlistEntry> pollWaitlist(Long courseId) {

        ArrayDeque<WaitlistEntry> queue = waitlists.get(courseId);

        if (queue == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(queue.pollFirst());
    }

    public boolean isWaitlisted(Long courseId, Long studentId) {

        ArrayDeque<WaitlistEntry> queue = waitlists.get(courseId);

        return queue != null && queue.stream()
                .anyMatch(e -> e.getStudentId().equals(studentId));
    }

    public int getWaitlistCount(Long courseId) {

        ArrayDeque<WaitlistEntry> queue = waitlists.get(courseId);

        return queue == null ? 0 : queue.size();
    }

    // Return a snapshot without exposing the internal queue
    public List<WaitlistEntry> getWaitlist(Long courseId) {

        ArrayDeque<WaitlistEntry> queue = waitlists.get(courseId);

        if (queue == null) {
            return List.of();
        }

        return List.copyOf(queue);
    }

}

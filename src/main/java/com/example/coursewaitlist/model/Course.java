package com.example.coursewaitlist.model;

public class Course {
	
	private final Long courseId;
	private final String courseName;
	private final int capacity;
	
	
	
	public Course(Long courseId, String courseName, int capacity) {
		
		if (courseId == null || courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Invalid course details");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        
		this.courseId = courseId;
		this.courseName = courseName;
		this.capacity = capacity;
	}
	public Long getCourseId() {
		return courseId;
	}
	public String getCourseName() {
		return courseName;
	}
	public int getCapacity() {
		return capacity;
	}
	
	
}

package com.example.coursewaitlist.model;

public class Student {
	
	private final Long studentId;
	private final String studentName;
	private boolean prerequisiteCompleted;
	
	public Student(Long studentId, String studentName, boolean prerequisiteCompleted) {
		if (studentId == null || studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("Invalid student details");
        }
		this.studentId = studentId;
		this.studentName = studentName;
		this.prerequisiteCompleted = prerequisiteCompleted;
	}
	public boolean isPrerequisiteCompleted() {
		return prerequisiteCompleted;
	}
	public void setPrerequisiteCompleted(boolean prerequisiteCompleted) {
		this.prerequisiteCompleted = prerequisiteCompleted;
	}
	public Long getStudentId() {
		return studentId;
	}
	public String getStudentName() {
		return studentName;
	}
	
	
}

package com.example.coursewaitlist.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.coursewaitlist.model.Course;
import com.example.coursewaitlist.model.Student;
import com.example.coursewaitlist.repository.InMemoryRepo;

@Component
public class DataInitializer implements CommandLineRunner{


    private final InMemoryRepo repository;

    public DataInitializer(InMemoryRepo repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {

        // Create one course with capacity of 2
        Course course = new Course(
                1L,
                "Java Advanced Programming",
                2
        );

        repository.saveCourse(course);

        // Create sample students
        repository.saveStudent(
                new Student(101L, "Rahul", true)
        );

        repository.saveStudent(
                new Student(102L, "Amit", true)
        );

        repository.saveStudent(
                new Student(103L, "Priya", true)
        );

        repository.saveStudent(
                new Student(104L, "Neha", true)
        );

        repository.saveStudent(
                new Student(105L, "Karan", false)
        );

        System.out.println("CourseWaitlist sample data initialized!");
    }
}

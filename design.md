# Course Waitlist System – Design Document

## 1. Overview

The Course Waitlist System is a Spring Boot REST API that manages student enrollment for a course with limited capacity.

When a course reaches its capacity, additional eligible students are added to a FIFO waitlist. When an enrolled student drops the course, the earliest eligible waitlisted student is promoted.

## 2. Technology Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Maven
- JUnit 5
- Java Collections Framework
- In-memory storage

## 3. Architecture

The application follows a layered architecture:

- Controller: Exposes REST endpoints and handles HTTP requests.
- Service: Implements enrollment, drop, eligibility and promotion logic.
- Repository: Maintains course, student, enrollment and waitlist data.
- Model: Represents Course, Student, Enrollment and WaitlistEntry.
- DTO: Represents API requests and responses.
- Exception: Handles application errors globally.

## 4. Data Structures

| Data | Structure | Purpose |
|---|---|---|
| Courses | HashMap | Store courses by ID |
| Students | HashMap | Store students by ID |
| Enrollments | LinkedHashMap | Maintain enrollment insertion order |
| Waitlist | ArrayDeque | Maintain FIFO ordering |

## 5. Business Rules

1. A student must satisfy the prerequisite before enrollment.
2. A student cannot enroll in the same course more than once.
3. A student cannot have duplicate waitlist entries.
4. Enrollment cannot exceed course capacity.
5. Students are added to the end of the waitlist.
6. When a student drops, the earliest eligible waitlisted student is promoted.
7. Ineligible students are skipped and removed when they reach the front of the waitlist.
8. A student who is not enrolled cannot drop the course.
9. Repeated drops do not change enrollment or waitlist state.

## 6. Concurrency Handling

The EnrollmentService uses synchronized methods to serialize access to enrollment and waitlist operations within a single application instance.

This prevents concurrent requests handled by that instance from exceeding the course capacity.

For a multi-instance production deployment, shared database transactions and appropriate locking would be required.

## 7. API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /courses/{courseId}/enrollments | Enroll or waitlist a student |
| DELETE | /courses/{courseId}/enrollments/{studentId} | Drop a student |
| GET | /courses/{courseId}/enrollments | View enrolled students |
| GET | /courses/{courseId}/waitlist | View waitlisted students |

## 8. Testing Strategy

JUnit tests cover:

- Successful enrollment
- Course capacity
- Waitlist insertion
- Duplicate enrollment
- Duplicate waitlist requests
- Prerequisite validation
- FIFO promotion
- Skipping ineligible students
- Invalid and repeated drops
- Invalid IDs and missing records
- Concurrent enrollment requests

## 9. Limitations

- Data is stored in memory and is lost when the application restarts.
- The implementation supports a single application instance for concurrency safety.
- Authentication and authorization are not implemented.
- The application is designed as a focused assessment solution rather than a production-ready enrollment platform.

## 10. Future Improvements

- Persist data using a relational database.
- Use transactions and database locking for multi-instance deployments.
- Add authentication and authorization.
- Add structured logging and monitoring.
- Add integration tests for REST endpoints.

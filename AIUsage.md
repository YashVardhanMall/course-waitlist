# AI Usage Log

## 1. AI Tool Used
ChatGPT

## 2. Purpose of AI Assistance
- Understanding the assessment requirements.
- Planning the application architecture.
- Understanding Java collections for enrollment and FIFO waitlist management.
- Reviewing service-layer business logic.
- Creating JUnit test scenarios.
- Identifying edge cases and concurrency considerations.
- Preparing technical documentation.

## 3. AI Assistance and Verification

| Area | AI Assistance | My Action | Verification |
|---|---|---|---|
| Architecture | Suggested layered architecture | Reviewed and implemented | Application compilation |
| Data structures | Explained HashMap, LinkedHashMap and ArrayDeque | Implemented repository | Enrollment and FIFO tests |
| Enrollment | Suggested enrollment flow | Implemented and reviewed | JUnit tests |
| Waitlist | Suggested FIFO promotion logic | Implemented promotion logic | FIFO promotion tests |
| Eligibility | Suggested prerequisite validation | Implemented eligibility checks | Prerequisite tests |
| Concurrency | Explained synchronized methods | Implemented synchronization | Concurrent enrollment test |
| Edge cases | Suggested invalid ID and duplicate scenarios | Added validation and handling | JUnit tests |

## 4. Important Decisions

- Used in-memory storage instead of a database to keep the solution simple.
- Used ArrayDeque to implement FIFO waitlist behavior.
- Used synchronized service methods to protect state within one application instance.
- Ineligible waitlisted students are skipped and removed when they reach the front of the queue.
- Used JUnit 5 to validate business requirements.

## 5. Limitations

- Application data is lost on restart.
- Synchronization does not coordinate multiple application instances.
- No authentication or authorization is implemented.

## 6. Final Verification

- Maven compilation: Passed
- Unit tests: Passed
- Concurrency test: Passed
- REST API manual testing: To be confirmed

# Student Course Management App

A Spring Boot MVC application to manage **Students** and **Courses** with full Create, Read, and Update operations using JPA, JSP/JSTL views, and a dark-themed UI.

## Tech Stack

- **Spring Boot 3.2.5** — Web, Data JPA, Validation
- **H2 In-Memory Database** — Zero setup required
- **JSP + JSTL (Jakarta)** — Server-side views
- **Lombok** — Reduced boilerplate
- **JUnit 5 + Mockito** — Unit & integration tests

## Features

- 📋 **List** all students and courses
- ➕ **Create** new students/courses via forms
- ✏️ **Update** existing records
- 🔗 **Enrollment Join View** — JPQL INNER JOIN showing student-course pairs
- 🛡️ **Validation** — email uniqueness, field constraints with inline error messages
- 🌱 **Auto-seeded** — 10 Students + 10 Courses on startup

## Project Structure

```
src/
├── main/java/com/bits/studentcourse/
│   ├── entity/          Student.java, Course.java
│   ├── repository/      StudentRepository.java, CourseRepository.java
│   ├── service/         StudentService, CourseService (interface + impl)
│   ├── controller/      StudentController.java, CourseController.java
│   ├── dto/             EnrollmentDTO.java
│   └── config/          DataInitializer.java
└── main/webapp/WEB-INF/views/
    ├── students/        list.jsp, form.jsp, enrollments.jsp
    ├── courses/         list.jsp, form.jsp, enrolled.jsp
    └── common/          header.jsp, footer.jsp
```

## Running the Application

```bash
# Clone
git clone https://github.com/RajPrakash681/Student-Course-Management-App.git
cd Student-Course-Management-App

# Run (MUST use spring-boot:run for JSP support)
mvn spring-boot:run

# Access
http://localhost:8080/students   # Students list
http://localhost:8080/courses    # Courses list
http://localhost:8080/h2-console # H2 DB console (URL: jdbc:h2:mem:studentcoursedb, user: sa)

# Tests
mvn test
```

> ⚠️ **Important:** Use `mvn spring-boot:run` — JSPs do not work with `java -jar` in Spring Boot.

## Key Endpoints

| Method | URL | Description |
|---|---|---|
| GET | `/students` | List all students |
| GET | `/students/new` | Add student form |
| POST | `/students/save` | Save new student |
| GET | `/students/edit/{id}` | Edit student form |
| POST | `/students/update` | Update student |
| GET | `/students/enrollments` | Student-Course inner join view |
| GET | `/courses` | List all courses |
| GET | `/courses/new` | Add course form |
| POST | `/courses/save` | Save new course |
| GET | `/courses/edit/{id}` | Edit course form |
| POST | `/courses/update` | Update course |
| GET | `/courses/enrolled` | Courses with enrolled students |

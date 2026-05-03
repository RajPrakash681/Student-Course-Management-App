# Student–Course Management Application
## Spring Boot Assignment Report

**Student Name:** [Your Name]
**Registration No.:** [Your Reg. No.]
**Subject:** Object-Oriented Analysis & Design / Enterprise Application Development
**GitHub Repository:** https://github.com/RajPrakash681/Student-Course-Management-App

---

## 1. Introduction

This report documents the design and implementation of a **Spring Boot MVC web application** that manages two related entities: **Student** and **Course**. The application demonstrates core enterprise Java concepts including JPA-based persistence, layered architecture (Controller → Service → Repository), JSP-based views with JSTL, Bean Validation, and unit testing with JUnit 5 and Mockito.

### Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.2.5 | Application framework |
| Spring Data JPA | 3.2.5 | ORM / Data access |
| Hibernate | 6.x | JPA implementation |
| H2 Database | Runtime | In-memory relational DB |
| Tomcat Embed Jasper | — | JSP rendering |
| JSTL (Jakarta) | 3.0.1 | JSP tag library |
| Lombok | — | Boilerplate reduction |
| JUnit 5 + Mockito | — | Unit & integration testing |
| Maven | 3.x | Build tool |

---

## 2. Entity Relationship Design

### 2.1 Entities

**Student** — Represents a university student who can be enrolled in multiple courses.

| Attribute | Type | Constraint |
|---|---|---|
| id | Long (PK) | Auto-generated |
| name | String | Not blank, 2–100 chars |
| email | String | Not blank, valid format, unique |
| department | String | Not blank |
| enrollmentYear | int | 2000–2030 |

**Course** — Represents an academic course that can have multiple enrolled students.

| Attribute | Type | Constraint |
|---|---|---|
| id | Long (PK) | Auto-generated |
| title | String | Not blank, 2–150 chars |
| credits | int | 1–6 |
| instructor | String | Not blank |
| duration | String | Not blank |

### 2.2 Relationship

```
Student (*)  ──── student_course (join table) ────  (*) Course
               student_id (FK) | course_id (FK)
```

- **Type:** `@ManyToMany` — a student can enroll in many courses; a course can have many students.
- **Join Table:** `student_course` with columns `student_id` and `course_id`.
- **Owner Side:** `Student` owns the relationship (holds `@JoinTable`).
- **Inverse Side:** `Course` uses `@ManyToMany(mappedBy = "courses")`.

### 2.3 ER Diagram (ASCII)

```
+------------------+         +-------------------+         +----------------+
|    students      |         |  student_course   |         |    courses     |
+------------------+         +-------------------+         +----------------+
| PK id            |---1---<>| FK student_id     |         | PK id          |
| name             |         | FK course_id      |<>---1---| title          |
| email (UNIQUE)   |         +-------------------+         | credits        |
| department       |                                       | instructor     |
| enrollment_year  |                                       | duration       |
+------------------+                                       +----------------+
```

---

## 3. Project Architecture

The application follows the standard **Spring MVC layered architecture**:

```
Browser (HTTP)
     │
     ▼
┌─────────────────┐
│  Controller     │  StudentController, CourseController
│  (@Controller)  │  Handles HTTP routes, binds model to view
└────────┬────────┘
         │ calls
         ▼
┌─────────────────┐
│  Service Layer  │  StudentService, CourseService
│  (@Service)     │  Business logic, validation, transactions
└────────┬────────┘
         │ calls
         ▼
┌─────────────────┐
│  Repository     │  StudentRepository, CourseRepository
│  (@Repository)  │  JpaRepository + custom @Query methods
└────────┬────────┘
         │ uses
         ▼
┌─────────────────┐
│  Database (H2)  │  In-memory relational DB (students, courses, student_course)
└─────────────────┘
         ▲
         │ renders
┌─────────────────┐
│  View (JSP)     │  list.jsp, form.jsp, enrollments.jsp, enrolled.jsp
│  + JSTL         │  Styled with embedded CSS (dark theme)
└─────────────────┘
```

---

## 4. Implementation Details

### 4.1 Entity Classes

#### `Student.java` — Full Code

```java
package com.bits.studentcourse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "courses")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Department is required")
    @Column(nullable = false)
    private String department;

    @Min(value = 2000, message = "Enrollment year must be 2000 or later")
    @Max(value = 2030, message = "Enrollment year cannot be too far in the future")
    @Column(nullable = false)
    private int enrollmentYear;

    @ManyToMany(fetch = FetchType.LAZY,
                cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}
```

#### `Course.java` — Full Code

```java
package com.bits.studentcourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "students")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course title is required")
    @Size(min = 2, max = 150)
    @Column(nullable = false)
    private String title;

    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits cannot exceed 6")
    @Column(nullable = false)
    private int credits;

    @NotBlank(message = "Instructor name is required")
    @Column(nullable = false)
    private String instructor;

    @NotBlank(message = "Duration is required")
    @Column(nullable = false)
    private String duration;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Student> students = new ArrayList<>();
}
```

**Key design decisions:**
- `FetchType.LAZY` on the `@ManyToMany` to avoid unnecessary data loading.
- `CascadeType.PERSIST` and `CascadeType.MERGE` to propagate save/update operations.
- `unique = true` on email enforced at both the DB level and service layer.
- `@JsonIgnore` on the inverse side prevents infinite recursion in JSON serialization.

### 4.2 Repository Layer

Both repositories extend `JpaRepository<Entity, Long>`, gaining full CRUD + pagination out of the box.

#### `StudentRepository.java` — Full Code

```java
package com.bits.studentcourse.repository;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Derived query — find by unique email
    Optional<Student> findByEmail(String email);

    // Derived query — filter by department
    List<Student> findByDepartment(String department);

    // Derived query — filter by enrollment year
    List<Student> findByEnrollmentYear(int year);

    // INNER JOIN: fetches only students who have at least one course
    @Query("SELECT DISTINCT s FROM Student s JOIN FETCH s.courses c ORDER BY s.name")
    List<Student> findStudentsWithEnrollments();

    // Constructor expression DTO — flat result of inner join between Student and Course
    @Query("SELECT new com.bits.studentcourse.dto.EnrollmentDTO(" +
           "s.id, s.name, s.email, s.department, s.enrollmentYear, " +
           "c.id, c.title, c.instructor, c.credits, c.duration) " +
           "FROM Student s JOIN s.courses c ORDER BY s.name, c.title")
    List<EnrollmentDTO> findAllEnrollmentsAsDTO();

    // Students enrolled in courses with at least minCredits
    @Query("SELECT DISTINCT s FROM Student s JOIN s.courses c WHERE c.credits >= :minCredits")
    List<Student> findStudentsEnrolledInHighCreditCourses(@Param("minCredits") int minCredits);
}
```

#### `CourseRepository.java` — Full Code

```java
package com.bits.studentcourse.repository;

import com.bits.studentcourse.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Derived query — find by instructor name
    List<Course> findByInstructor(String instructor);

    // Derived query — find by exact credits
    List<Course> findByCredits(int credits);

    // Derived query — case-insensitive title search
    List<Course> findByTitleContainingIgnoreCase(String keyword);

    // INNER JOIN: fetches only courses with at least one enrolled student
    @Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.students s ORDER BY c.title")
    List<Course> findCoursesWithEnrolledStudents();

    // Courses with credits at or above a minimum threshold
    @Query("SELECT c FROM Course c WHERE c.credits >= :minCredits ORDER BY c.credits DESC")
    List<Course> findByMinCredits(@Param("minCredits") int minCredits);
}
```

The `JOIN FETCH` eagerly loads the collection in a single SQL query, preventing the N+1 problem. The `DISTINCT` keyword removes duplicate rows caused by the join.

### 4.3 Service Layer

The service layer handles all business logic and sits between the controller and repository.

#### `StudentServiceImpl.java` — Full Code

```java
package com.bits.studentcourse.service;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;
import com.bits.studentcourse.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Override
    public Student saveStudent(Student student) {
        // Guard: reject duplicate email before hitting DB constraint
        studentRepository.findByEmail(student.getEmail()).ifPresent(existing -> {
            throw new DataIntegrityViolationException(
                "A student with email '" + student.getEmail() + "' already exists.");
        });
        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Student student) {
        Optional<Student> existing = studentRepository.findByEmail(student.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(student.getId())) {
            throw new DataIntegrityViolationException(
                "Another student already uses email '" + student.getEmail() + "'.");
        }
        return studentRepository.save(student);
    }

    @Override
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getStudentsWithEnrollments() {
        return studentRepository.findStudentsWithEnrollments();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments() {
        return studentRepository.findAllEnrollmentsAsDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return studentRepository.findByEmail(email).isPresent();
    }
}
```

#### `CourseServiceImpl.java` — Full Code

```java
package com.bits.studentcourse.service;

import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesWithEnrolledStudents() {
        return courseRepository.findCoursesWithEnrolledStudents();
    }
}
```

- All write operations are `@Transactional` (inherited from class annotation).
- Read operations use `@Transactional(readOnly = true)` for performance.
- `DataIntegrityViolationException` is thrown in the service and caught in the controller.

### 4.4 Controller Layer

#### `StudentController.java` — Full Code

```java
package com.bits.studentcourse.controller;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;
import com.bits.studentcourse.service.CourseService;
import com.bits.studentcourse.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;

    // READ: List all students
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("pageTitle", "All Students");
        return "students/list";
    }

    // CREATE: Show blank form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("allCourses", courseService.getAllCourses());
        model.addAttribute("pageTitle", "Add New Student");
        model.addAttribute("formAction", "/students/save");
        return "students/form";
    }

    // CREATE: Handle form submission
    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                              BindingResult bindingResult, Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Add New Student");
            model.addAttribute("formAction", "/students/save");
            return "students/form";
        }
        try {
            studentService.saveStudent(student);
            redirectAttributes.addFlashAttribute("successMessage",
                "Student '" + student.getName() + "' added successfully!");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("formAction", "/students/save");
            return "students/form";
        }
        return "redirect:/students";  // PRG pattern
    }

    // UPDATE: Show pre-filled form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return studentService.getStudentById(id).map(student -> {
            model.addAttribute("student", student);
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Edit Student");
            model.addAttribute("formAction", "/students/update");
            return "students/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Student with ID " + id + " not found.");
            return "redirect:/students";
        });
    }

    // UPDATE: Handle update submission
    @PostMapping("/update")
    public String updateStudent(@Valid @ModelAttribute("student") Student student,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Edit Student");
            model.addAttribute("formAction", "/students/update");
            return "students/form";
        }
        try {
            studentService.updateStudent(student);
            redirectAttributes.addFlashAttribute("successMessage",
                "Student '" + student.getName() + "' updated successfully!");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("formAction", "/students/update");
            return "students/form";
        }
        return "redirect:/students";
    }

    // READ: Enrollment join view
    @GetMapping("/enrollments")
    public String showEnrollments(Model model) {
        List<EnrollmentDTO> enrollments = studentService.getAllEnrollments();
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("pageTitle", "Student Enrollments (Inner Join)");
        return "students/enrollments";
    }
}
```

**Key patterns used:**
- **Post-Redirect-Get (PRG):** Redirects after successful save/update to prevent duplicate form submissions on refresh.
- **Flash attributes:** `RedirectAttributes.addFlashAttribute()` persists success messages across the redirect.
- **BindingResult:** Captures `@Valid` Bean Validation errors and returns them inline on the form.
- **Shared form:** Same `form.jsp` handles both Create and Update via dynamic `formAction` and hidden `id`.

### 4.5 View Layer (JSP)

All JSP pages include a shared `header.jsp` (which contains all CSS) and `footer.jsp`.

#### `students/list.jsp` — Key Sections

```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success">✅ ${successMessage}</div>
</c:if>

<c:forEach var="s" items="${students}" varStatus="loop">
    <tr>
        <td>${loop.index + 1}</td>
        <td><strong>${s.name}</strong></td>
        <td>${s.email}</td>
        <td><span class="badge badge-purple">${s.department}</span></td>
        <td>${s.enrollmentYear}</td>
        <td><span class="badge badge-blue">${s.courses.size()} enrolled</span></td>
        <td><a href="/students/edit/${s.id}" class="btn btn-edit btn-sm">Edit</a></td>
    </tr>
</c:forEach>
```

#### `students/form.jsp` — Key Sections (shared for Create & Update)

```jsp
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form action="${formAction}" method="post" modelAttribute="student">
    <form:hidden path="id"/>

    <div class="form-group">
        <label>Full Name *</label>
        <form:input path="name" placeholder="e.g. Alice Johnson"/>
        <form:errors path="name" cssClass="field-error"/>
    </div>

    <div class="form-group">
        <label>Email *</label>
        <form:input path="email" type="email"/>
        <form:errors path="email" cssClass="field-error"/>
    </div>

    <div class="form-group">
        <label>Courses (hold Ctrl to select multiple)</label>
        <form:select path="courses" multiple="true"
                     items="${allCourses}" itemValue="id" itemLabel="title"/>
    </div>

    <button type="submit" class="btn btn-primary">
        ${empty student.id ? 'Add Student' : 'Update Student'}
    </button>
</form:form>
```

#### `students/enrollments.jsp` — Inner Join Table

```jsp
<c:forEach var="e" items="${enrollments}" varStatus="loop">
    <tr>
        <td>${loop.index + 1}</td>
        <td><strong>${e.studentName}</strong></td>
        <td>${e.studentEmail}</td>
        <td>${e.department}</td>
        <td>${e.enrollmentYear}</td>
        <td><strong>${e.courseTitle}</strong></td>
        <td>${e.instructor}</td>
        <td>${e.credits} cr</td>
        <td>${e.duration}</td>
    </tr>
</c:forEach>
```

The same `formAction` + hidden `id` pattern lets one JSP form serve both Create and Update operations.

### 4.6 CSS Design

All pages share a consistent **dark-theme design** with:
- Dark gradient background (`#0d0d1a`)
- Fixed sidebar navigation with smooth hover effects
- Card-based layout for forms and tables
- Purple/blue gradient accents (`#6c63ff` → `#a855f7`)
- Responsive grid layouts for form fields
- Badge components for labels (department, credits, enrollment count)

### 4.7 Database Seeding

#### `DataInitializer.java` — Full Code

```java
package com.bits.studentcourse.config;

import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.entity.Student;
import com.bits.studentcourse.repository.CourseRepository;
import com.bits.studentcourse.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            log.info("Database already populated, skipping seed.");
            return;
        }

        // Create 10 Courses
        Course c1  = courseRepository.save(new Course(null, "Introduction to Programming",  4, "Dr. Alan Turing",     "16 weeks", List.of()));
        Course c2  = courseRepository.save(new Course(null, "Data Structures & Algorithms", 4, "Dr. Donald Knuth",    "16 weeks", List.of()));
        Course c3  = courseRepository.save(new Course(null, "Calculus I",                   3, "Dr. Isaac Newton",    "16 weeks", List.of()));
        Course c4  = courseRepository.save(new Course(null, "Linear Algebra",               3, "Dr. Carl Gauss",      "14 weeks", List.of()));
        Course c5  = courseRepository.save(new Course(null, "Database Systems",             4, "Dr. Edgar Codd",      "16 weeks", List.of()));
        Course c6  = courseRepository.save(new Course(null, "Operating Systems",            4, "Dr. Linus Torvalds",  "16 weeks", List.of()));
        Course c7  = courseRepository.save(new Course(null, "Computer Networks",            3, "Dr. Vint Cerf",       "14 weeks", List.of()));
        Course c8  = courseRepository.save(new Course(null, "Web Development",              3, "Dr. Tim Berners-Lee", "12 weeks", List.of()));
        Course c9  = courseRepository.save(new Course(null, "Machine Learning",             4, "Dr. Andrew Ng",       "16 weeks", List.of()));
        Course c10 = courseRepository.save(new Course(null, "Software Engineering",         3, "Dr. Fred Brooks",     "14 weeks", List.of()));

        // Create 10 Students with enrollments
        createStudent("Alice Johnson",  "alice.johnson@bits.edu",  "Computer Science", 2022, c1, c2, c5);
        createStudent("Bob Smith",      "bob.smith@bits.edu",      "Mathematics",      2021, c3, c4, c9);
        createStudent("Carol Davis",    "carol.davis@bits.edu",    "Physics",          2023, c3, c4, c6);
        createStudent("David Wilson",   "david.wilson@bits.edu",   "Computer Science", 2022, c1, c5, c10);
        createStudent("Emma Brown",     "emma.brown@bits.edu",     "Computer Science", 2021, c2, c7, c8);
        createStudent("Frank Miller",   "frank.miller@bits.edu",   "Engineering",      2023, c6, c7, c10);
        createStudent("Grace Lee",      "grace.lee@bits.edu",      "Biology",          2022, c3, c9);
        createStudent("Henry Taylor",   "henry.taylor@bits.edu",   "Physics",          2021, c1, c4, c6);
        createStudent("Iris Martinez",  "iris.martinez@bits.edu",  "Mathematics",      2023, c3, c8, c9);
        createStudent("Jack Anderson",  "jack.anderson@bits.edu",  "Computer Science", 2022, c1, c2, c5, c7);

        log.info("Seeded 10 students and 10 courses.");
    }

    private void createStudent(String name, String email, String dept, int year, Course... courses) {
        Student s = new Student();
        s.setName(name);
        s.setEmail(email);
        s.setDepartment(dept);
        s.setEnrollmentYear(year);
        for (Course c : courses) s.getCourses().add(c);
        studentRepository.save(s);
    }
}
```

---

## 5. CRUD Operations

### 5.1 Create Operation

1. User navigates to `/students/new` or `/courses/new`.
2. Spring renders the form JSP with an empty model object.
3. User fills the form and clicks **Add Student / Add Course**.
4. The form POSTs to `/students/save` or `/courses/save`.
5. `@Valid` triggers Bean Validation; errors are shown inline if any.
6. If valid, `StudentService.saveStudent()` checks for duplicate emails.
7. On success, user is redirected to the list page with a success flash message.
8. On email conflict, a meaningful error message is shown on the form.

### 5.2 Read Operation

- `GET /students` → `StudentController.listStudents()` → fetches all students via service → renders `students/list.jsp`
- `GET /students/enrollments` → fetches inner-join DTOs → renders `students/enrollments.jsp`
- `GET /courses/enrolled` → fetches courses with enrolled students (inner join) → renders `courses/enrolled.jsp`

### 5.3 Update Operation

1. User clicks **Edit** on any row → `GET /students/edit/{id}`.
2. Controller fetches the student by ID and sets it as the model attribute.
3. Form JSP pre-populates all fields from the bound model.
4. User edits fields and submits → `POST /students/update`.
5. Service validates and saves; the hidden `id` field ensures UPDATE not INSERT.

---

## 6. Unit Testing

### 6.1 Test Strategy

| Test Class | Type | Framework | What is Tested |
|---|---|---|---|
| `StudentServiceImplTest` | Unit | JUnit 5 + Mockito | Business logic, duplicate email guard |
| `CourseServiceImplTest` | Unit | JUnit 5 + Mockito | CRUD delegation to repository |
| `StudentRepositoryTest` | Integration | @DataJpaTest + H2 | Custom JPQL queries, derived queries |
| `CourseRepositoryTest` | Integration | @DataJpaTest + H2 | Custom JPQL queries, inner-join |

### 6.2 `StudentServiceImplTest.java` — Full Code

```java
package com.bits.studentcourse.service;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;
import com.bits.studentcourse.repository.StudentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentServiceImpl Unit Tests")
class StudentServiceImplTest {

    @Mock private StudentRepository studentRepository;
    @InjectMocks private StudentServiceImpl studentService;

    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setName("Alice Johnson");
        mockStudent.setEmail("alice@bits.edu");
        mockStudent.setDepartment("Computer Science");
        mockStudent.setEnrollmentYear(2022);
    }

    @Test
    @DisplayName("getAllStudents() should return all students from repository")
    void getAllStudents_returnsAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(mockStudent));
        List<Student> result = studentService.getAllStudents();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getStudentById() should return student when found")
    void getStudentById_found() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        Optional<Student> result = studentService.getStudentById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStudentById() should return empty when not found")
    void getStudentById_notFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(studentService.getStudentById(99L)).isEmpty();
    }

    @Test
    @DisplayName("saveStudent() should save and return student when email is unique")
    void saveStudent_success() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);
        Student result = studentService.saveStudent(mockStudent);
        assertThat(result.getName()).isEqualTo("Alice Johnson");
        verify(studentRepository).save(mockStudent);
    }

    @Test
    @DisplayName("saveStudent() should throw DataIntegrityViolationException on duplicate email")
    void saveStudent_duplicateEmail_throwsException() {
        when(studentRepository.findByEmail("alice@bits.edu"))
            .thenReturn(Optional.of(mockStudent));
        assertThatThrownBy(() -> studentService.saveStudent(mockStudent))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessageContaining("alice@bits.edu");
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStudent() should update when email belongs to same student")
    void updateStudent_sameEmail_success() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);
        mockStudent.setName("Alice Updated");
        Student result = studentService.updateStudent(mockStudent);
        assertThat(result).isNotNull();
        verify(studentRepository).save(mockStudent);
    }

    @Test
    @DisplayName("updateStudent() should throw when email is used by a different student")
    void updateStudent_emailConflict_throwsException() {
        Student other = new Student();
        other.setId(2L);
        other.setEmail("alice@bits.edu");
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(other));
        mockStudent.setId(1L);
        assertThatThrownBy(() -> studentService.updateStudent(mockStudent))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("getAllEnrollments() should return flat DTO list from repository")
    void getAllEnrollments_returnsDTOList() {
        EnrollmentDTO dto = new EnrollmentDTO(1L, "Alice", "alice@bits.edu",
                "CS", 2022, 1L, "Data Structures", "Dr. Knuth", 4, "16 weeks");
        when(studentRepository.findAllEnrollmentsAsDTO()).thenReturn(List.of(dto));
        List<EnrollmentDTO> result = studentService.getAllEnrollments();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseTitle()).isEqualTo("Data Structures");
    }

    @Test
    @DisplayName("existsByEmail() should return true when email exists")
    void existsByEmail_true() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(mockStudent));
        assertThat(studentService.existsByEmail("alice@bits.edu")).isTrue();
    }
}
```

### 6.3 `CourseServiceImplTest.java` — Full Code

```java
package com.bits.studentcourse.service;

import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.repository.CourseRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseServiceImpl Unit Tests")
class CourseServiceImplTest {

    @Mock private CourseRepository courseRepository;
    @InjectMocks private CourseServiceImpl courseService;

    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockCourse = new Course();
        mockCourse.setId(1L);
        mockCourse.setTitle("Introduction to Programming");
        mockCourse.setCredits(4);
        mockCourse.setInstructor("Dr. Alan Turing");
        mockCourse.setDuration("16 weeks");
    }

    @Test
    @DisplayName("getAllCourses() should return all courses")
    void getAllCourses_returnsAll() {
        when(courseRepository.findAll()).thenReturn(List.of(mockCourse));
        List<Course> result = courseService.getAllCourses();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Introduction to Programming");
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getCourseById() returns course when found")
    void getCourseById_found() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(mockCourse));
        Optional<Course> result = courseService.getCourseById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getCredits()).isEqualTo(4);
    }

    @Test
    @DisplayName("saveCourse() should persist and return the saved course")
    void saveCourse_success() {
        when(courseRepository.save(any(Course.class))).thenReturn(mockCourse);
        Course result = courseService.saveCourse(mockCourse);
        assertThat(result.getTitle()).isEqualTo("Introduction to Programming");
        verify(courseRepository).save(mockCourse);
    }

    @Test
    @DisplayName("updateCourse() should save updated course and return it")
    void updateCourse_success() {
        mockCourse.setTitle("Advanced Programming");
        when(courseRepository.save(any(Course.class))).thenReturn(mockCourse);
        Course result = courseService.updateCourse(mockCourse);
        assertThat(result.getTitle()).isEqualTo("Advanced Programming");
    }

    @Test
    @DisplayName("deleteCourse() should call deleteById on repository")
    void deleteCourse_callsRepository() {
        doNothing().when(courseRepository).deleteById(1L);
        courseService.deleteCourse(1L);
        verify(courseRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("getCoursesWithEnrolledStudents() returns courses from inner join query")
    void getCoursesWithEnrolledStudents_returnsList() {
        when(courseRepository.findCoursesWithEnrolledStudents()).thenReturn(List.of(mockCourse));
        List<Course> result = courseService.getCoursesWithEnrolledStudents();
        assertThat(result).hasSize(1);
        verify(courseRepository).findCoursesWithEnrolledStudents();
    }
}
```

### 6.4 `StudentRepositoryTest.java` — Full Code (@DataJpaTest)

```java
package com.bits.studentcourse.repository;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.entity.Student;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("StudentRepository Integration Tests")
class StudentRepositoryTest {

    @Autowired private StudentRepository studentRepository;
    @Autowired private CourseRepository courseRepository;

    private Student savedStudent;
    private Course savedCourse;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        savedCourse = courseRepository.save(
            new Course(null, "Data Structures", 4, "Dr. Knuth", "16 weeks", List.of()));
        savedStudent = new Student();
        savedStudent.setName("Alice Johnson");
        savedStudent.setEmail("alice@test.com");
        savedStudent.setDepartment("Computer Science");
        savedStudent.setEnrollmentYear(2022);
        savedStudent.getCourses().add(savedCourse);
        savedStudent = studentRepository.save(savedStudent);
    }

    @Test
    @DisplayName("findByEmail() should return student with matching email")
    void findByEmail_found() {
        Optional<Student> result = studentRepository.findByEmail("alice@test.com");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice Johnson");
    }

    @Test
    @DisplayName("findByDepartment() should return students in specified department")
    void findByDepartment() {
        List<Student> result = studentRepository.findByDepartment("Computer Science");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findStudentsWithEnrollments() INNER JOIN returns only enrolled students")
    void findStudentsWithEnrollments_returnsEnrolledOnly() {
        Student noCoursesStudent = new Student();
        noCoursesStudent.setName("Bob NoEnroll");
        noCoursesStudent.setEmail("bob@test.com");
        noCoursesStudent.setDepartment("Physics");
        noCoursesStudent.setEnrollmentYear(2023);
        studentRepository.save(noCoursesStudent);

        List<Student> result = studentRepository.findStudentsWithEnrollments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
        assertThat(result.get(0).getCourses()).isNotEmpty();
    }

    @Test
    @DisplayName("findAllEnrollmentsAsDTO() returns flat DTO with joined student-course data")
    void findAllEnrollmentsAsDTO_returnsCorrectDTO() {
        List<EnrollmentDTO> dtos = studentRepository.findAllEnrollmentsAsDTO();
        assertThat(dtos).hasSize(1);
        EnrollmentDTO dto = dtos.get(0);
        assertThat(dto.getStudentName()).isEqualTo("Alice Johnson");
        assertThat(dto.getCourseTitle()).isEqualTo("Data Structures");
        assertThat(dto.getCredits()).isEqualTo(4);
    }
}
```

### 6.5 `CourseRepositoryTest.java` — Full Code (@DataJpaTest)

```java
package com.bits.studentcourse.repository;

import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.entity.Student;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("CourseRepository Integration Tests")
class CourseRepositoryTest {

    @Autowired private CourseRepository courseRepository;
    @Autowired private StudentRepository studentRepository;

    private Course savedCourse;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        savedCourse = courseRepository.save(
            new Course(null, "Machine Learning", 4, "Dr. Andrew Ng", "16 weeks", List.of()));
        courseRepository.save(
            new Course(null, "Calculus I", 3, "Dr. Newton", "16 weeks", List.of()));
    }

    @Test
    @DisplayName("findAll() should return all persisted courses")
    void findAll_returnsAll() {
        assertThat(courseRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("findByInstructor() should return courses by instructor")
    void findByInstructor() {
        List<Course> result = courseRepository.findByInstructor("Dr. Andrew Ng");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Machine Learning");
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase() should do case-insensitive search")
    void findByTitleContaining() {
        List<Course> result = courseRepository.findByTitleContainingIgnoreCase("machine");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findCoursesWithEnrolledStudents() returns only courses with students")
    void findCoursesWithEnrolledStudents_innerJoin() {
        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("test@bits.edu");
        student.setDepartment("CS");
        student.setEnrollmentYear(2023);
        student.getCourses().add(savedCourse);
        studentRepository.save(student);

        List<Course> result = courseRepository.findCoursesWithEnrolledStudents();

        // Only Machine Learning has a student; Calculus I does not
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Machine Learning");
    }

    @Test
    @DisplayName("findByMinCredits() should return courses with credits >= threshold")
    void findByMinCredits() {
        List<Course> result = courseRepository.findByMinCredits(4);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCredits()).isGreaterThanOrEqualTo(4);
    }
}

---

## 7. How to Run

### Prerequisites
- Java 17+
- Maven 3.6+
- No external database needed (H2 in-memory)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/RajPrakash681/Student-Course-Management-App.git
cd Student-Course-Management-App

# 2. Run the application
mvn spring-boot:run

# 3. Open browser
http://localhost:8080/students

# 4. H2 Console (optional)
http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:studentcoursedb
# Username: sa | Password: (blank)

# 5. Run tests
mvn test
```

---

## 8. Screenshots

> **[INSTRUCTIONS FOR PDF]:** Run the application using `mvn spring-boot:run`, open your browser, and take screenshots of the following pages. Paste them below.

**Screenshot 1 — Students List Page (`/students`)**
> _[Paste screenshot here — shows the 10 seeded students in the dark-themed table]_

**Screenshot 2 — Add Student Form (`/students/new`)**
> _[Paste screenshot here — shows the create form with input fields]_

**Screenshot 3 — Validation Error on Form**
> _[Paste screenshot here — submit the form empty to trigger validation errors]_

**Screenshot 4 — Duplicate Email Error**
> _[Paste screenshot here — try adding a student with an existing email]_

**Screenshot 5 — Edit Student Form (`/students/edit/1`)**
> _[Paste screenshot here — shows the pre-filled update form]_

**Screenshot 6 — Enrollment Join View (`/students/enrollments`)**
> _[Paste screenshot here — shows the INNER JOIN table with student-course pairs]_

**Screenshot 7 — Courses List Page (`/courses`)**
> _[Paste screenshot here — shows the 10 seeded courses]_

**Screenshot 8 — Courses with Enrolled Students (`/courses/enrolled`)**
> _[Paste screenshot here — shows the inner join result for courses]_

**Screenshot 9 — Test Results (`mvn test`)**
> _[Paste terminal screenshot here showing all tests passing]_

---

## 9. Challenges Faced and Solutions

### Challenge 1: JSP Not Rendering with Spring Boot JAR Packaging
**Problem:** Spring Boot's executable JAR does not support JSPs at runtime because the servlet container cannot access the JSP files inside a nested JAR.

**Solution:** Used `mvn spring-boot:run` for development, which runs in exploded mode and supports JSPs. Added `tomcat-embed-jasper` as a regular (not provided) dependency so it is available at runtime. The JSP view resolver is configured via `spring.mvc.view.prefix` and `spring.mvc.view.suffix` in `application.properties`.

---

### Challenge 2: JSTL Tag URI Changed in Jakarta EE 10 (Spring Boot 3.x)
**Problem:** The classic JSTL URI `http://java.sun.com/jsp/jstl/core` causes a `JasperException` in Spring Boot 3.x, which uses Jakarta EE 10.

**Solution:** Updated the taglib declaration to use the new Jakarta namespace:
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
```
And used the correct Maven dependency:
```xml
<dependency>
    <groupId>org.glassfish.web</groupId>
    <artifactId>jakarta.servlet.jsp.jstl</artifactId>
    <version>3.0.1</version>
</dependency>
```

---

### Challenge 3: N+1 Query Problem with Lazy-Loaded ManyToMany
**Problem:** Accessing `student.getCourses()` in JSP inside a `forEach` loop fired a separate SQL query per student (N+1 problem), causing performance issues.

**Solution:** Used `JOIN FETCH` in the JPQL query to eagerly load courses in a single SQL query:
```java
@Query("SELECT DISTINCT s FROM Student s JOIN FETCH s.courses c ORDER BY s.name")
List<Student> findStudentsWithEnrollments();
```
The `DISTINCT` keyword prevents duplicate student rows from the join.

---

### Challenge 4: Shared Form for Create and Update
**Problem:** Maintaining two separate JSP forms (one for create, one for update) leads to code duplication.

**Solution:** Designed a single `form.jsp` that works for both operations by:
- Passing a `formAction` attribute from the controller (`/students/save` for create, `/students/update` for update).
- Including a `<form:hidden path="id"/>` field so the ID is submitted during updates.
- Dynamically changing the submit button label using EL: `${empty student.id ? 'Add' : 'Update'}`.

---

### Challenge 5: Flash Messages Surviving Redirect (PRG Pattern)
**Problem:** After a successful save, a redirect clears the model, so success messages were lost.

**Solution:** Used Spring MVC's `RedirectAttributes.addFlashAttribute()` which stores the message in the session for exactly one redirect:
```java
redirectAttributes.addFlashAttribute("successMessage", "Student added!");
return "redirect:/students";
```
The JSP then conditionally displays it with `<c:if test="${not empty successMessage}">`.

---

## 10. Conclusion

This application demonstrates a complete Spring Boot MVC project with:
- **JPA entities** with proper relationship annotations
- **Layered architecture** (Controller → Service → Repository)
- **Custom JPQL queries** including inner joins returning DTO projections
- **Bean Validation** integrated with Spring MVC form binding
- **JSP + JSTL views** with a modern dark-themed CSS design
- **Unit and integration tests** covering all layers

The code is clean, follows SOLID principles (single responsibility, dependency injection via interfaces), and applies real-world patterns like PRG, DTO projection, and transactional boundary management.

---

**GitHub Repository:** https://github.com/RajPrakash681/Student-Course-Management-App

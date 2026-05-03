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

The entities use JPA annotations to define the object-relational mapping:

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Email
    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> courses = new ArrayList<>();
}
```

**Key design decisions:**
- `FetchType.LAZY` on the `@ManyToMany` to avoid unnecessary data loading.
- `CascadeType.PERSIST` and `CascadeType.MERGE` to propagate save/update operations.
- `unique = true` on email enforced at both the DB level and service layer.

### 4.2 Repository Layer

Both repositories extend `JpaRepository<Entity, Long>`, gaining full CRUD + pagination out of the box.

**Custom JPQL Queries:**

```java
// 1. INNER JOIN — Returns only students who have at least one course
@Query("SELECT DISTINCT s FROM Student s JOIN FETCH s.courses c ORDER BY s.name")
List<Student> findStudentsWithEnrollments();

// 2. Constructor expression — Returns flat DTO with joined Student + Course data
@Query("SELECT new com.bits.studentcourse.dto.EnrollmentDTO(" +
       "s.id, s.name, s.email, s.department, s.enrollmentYear, " +
       "c.id, c.title, c.instructor, c.credits, c.duration) " +
       "FROM Student s JOIN s.courses c ORDER BY s.name, c.title")
List<EnrollmentDTO> findAllEnrollmentsAsDTO();
```

The `JOIN FETCH` in the first query eagerly loads the `courses` collection in a single SQL query, preventing the N+1 problem.

### 4.3 Service Layer

The service layer handles business logic between the controller and repository:

```java
@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    public Student saveStudent(Student student) {
        // Guard: duplicate email check before saving
        studentRepository.findByEmail(student.getEmail()).ifPresent(existing -> {
            throw new DataIntegrityViolationException(
                "A student with email '" + student.getEmail() + "' already exists.");
        });
        return studentRepository.save(student);
    }
}
```

- All write operations are `@Transactional` (inherited from class).
- Read operations use `@Transactional(readOnly = true)` for performance.
- `DataIntegrityViolationException` is caught in the controller and shown to the user.

### 4.4 Controller Layer

Controllers use Spring MVC's `@Controller` + `@RequestMapping` and bind form data using `@ModelAttribute`:

```java
@PostMapping("/save")
public String saveStudent(@Valid @ModelAttribute("student") Student student,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        // Return to form with errors shown inline
        return "students/form";
    }
    try {
        studentService.saveStudent(student);
        redirectAttributes.addFlashAttribute("successMessage", "Student added!");
    } catch (DataIntegrityViolationException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "students/form";
    }
    return "redirect:/students"; // PRG pattern
}
```

**Key patterns used:**
- **Post-Redirect-Get (PRG):** After successful save/update, redirect to the list page to prevent duplicate submissions.
- **Flash attributes:** Success messages survive the redirect via `RedirectAttributes`.
- **BindingResult:** Validation errors from `@Valid` are captured and displayed back on the form.

### 4.5 View Layer (JSP)

JSP pages use **JSTL** for logic and **Spring Form tags** for data binding.

**Listing students (JSTL `<c:forEach>`):**
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:forEach var="s" items="${students}" varStatus="loop">
    <tr>
        <td>${loop.index + 1}</td>
        <td>${s.name}</td>
        <td>${s.email}</td>
        <td>${s.department}</td>
        <td>${s.enrollmentYear}</td>
        <td>${s.courses.size()} enrolled</td>
        <td><a href="/students/edit/${s.id}">Edit</a></td>
    </tr>
</c:forEach>
```

**Form binding (Spring Form tags):**
```jsp
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<form:form action="${formAction}" method="post" modelAttribute="student">
    <form:input path="name" />
    <form:errors path="name" cssClass="field-error"/>
</form:form>
```

The same `form.jsp` is shared for both Create and Update by:
- Passing different `formAction` values (`/students/save` vs `/students/update`).
- The `id` field is hidden so it is submitted during update.

### 4.6 CSS Design

All pages share a consistent **dark-theme design** with:
- Dark gradient background (`#0d0d1a`)
- Fixed sidebar navigation with smooth hover effects
- Card-based layout for forms and tables
- Purple/blue gradient accents (`#6c63ff` → `#a855f7`)
- Responsive grid layouts for form fields
- Badge components for labels (department, credits, enrollment count)

### 4.7 Database Seeding

On application startup, `DataInitializer` (a `CommandLineRunner` bean) inserts:
- **10 Courses**: Introduction to Programming, Data Structures, Calculus I, Linear Algebra, Database Systems, Operating Systems, Computer Networks, Web Development, Machine Learning, Software Engineering
- **10 Students**: Alice Johnson, Bob Smith, Carol Davis, David Wilson, Emma Brown, Frank Miller, Grace Lee, Henry Taylor, Iris Martinez, Jack Anderson — each enrolled in 2–4 courses.

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

### 6.2 Sample Test — Duplicate Email Guard

```java
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
```

### 6.3 Sample Test — Inner Join Query

```java
@Test
@DisplayName("findStudentsWithEnrollments() INNER JOIN returns only enrolled students")
void findStudentsWithEnrollments_returnsEnrolledOnly() {
    // Bob has NO courses, Alice HAS a course
    List<Student> result = studentRepository.findStudentsWithEnrollments();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
}
```

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

# Student–Course Management Application
## Spring Boot Assignment Report

**Student Name:** [Your Name]
**Registration No.:** [Your Reg. No.]
**Subject:** Object-Oriented Analysis & Design / Enterprise Application Development
**GitHub Repository:** https://github.com/RajPrakash681/Student-Course-Management-App

---

## 1. Introduction

This report documents the design and implementation of a **Spring Boot MVC web application** that manages two related entities: **Student** and **Course**. The application demonstrates JPA-based persistence, layered architecture (Controller → Service → Repository), JSP-based views with JSTL, Bean Validation, and unit testing with JUnit 5 and Mockito.

### Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.2.5 | Core application framework |
| Spring Data JPA / Hibernate | 6.x | ORM and database access |
| H2 Database | Runtime | In-memory relational database |
| Tomcat Embed Jasper + JSTL | 3.0.1 | JSP rendering and tag library |
| Lombok | — | Reduces boilerplate (getters, setters, constructors) |
| JUnit 5 + Mockito | — | Unit and integration testing |
| Maven | 3.x | Build tool |

---

## 2. Entity Relationship Design

### 2.1 Entities

**Student** — Represents a university student who can be enrolled in multiple courses.

| Attribute | Type | Constraint |
|---|---|---|
| id | Long (PK) | Auto-generated |
| name | String | Not blank, 2–100 chars |
| email | String | Not blank, valid format, **unique** |
| department | String | Not blank |
| enrollmentYear | int | 2000–2030 |

**Course** — Represents an academic course that can have multiple enrolled students.

| Attribute | Type | Constraint |
|---|---|---|
| id | Long (PK) | Auto-generated |
| title | String | Not blank, 2–150 chars |
| credits | int | 1–6 |
| instructor | String | Not blank |
| duration | String | Not blank (e.g., "16 weeks") |

### 2.2 Relationship

```
Student (*)  ──── student_course (join table) ────  (*) Course
               student_id (FK) | course_id (FK)
```

- **Type:** `@ManyToMany` — a student can enroll in many courses; a course can have many students.
- **Join Table:** `student_course` with `student_id` and `course_id` foreign keys.
- **Owner Side:** `Student` holds `@JoinTable`.
- **Inverse Side:** `Course` uses `@ManyToMany(mappedBy = "courses")`.

### 2.3 ER Diagram

```
+------------------+       +-------------------+       +----------------+
|    students      |       |  student_course   |       |    courses     |
+------------------+       +-------------------+       +----------------+
| PK id            |--1--<>| FK student_id     |       | PK id          |
| name             |       | FK course_id      |<>--1--| title          |
| email (UNIQUE)   |       +-------------------+       | credits        |
| department       |                                   | instructor     |
| enrollment_year  |                                   | duration       |
+------------------+                                   +----------------+
```

---

## 3. Project Architecture

The application follows the standard **Spring MVC layered architecture**:

```
Browser  →  Controller  →  Service  →  Repository  →  H2 Database
                 ↑                                          |
               JSP View  ←─────────────────────────────────┘
```

| Layer | Class | Responsibility |
|---|---|---|
| Controller | `StudentController`, `CourseController` | Handle HTTP requests, bind model to view |
| Service | `StudentServiceImpl`, `CourseServiceImpl` | Business logic, validation, transactions |
| Repository | `StudentRepository`, `CourseRepository` | Data access, custom JPQL queries |
| View | JSP + JSTL | Render HTML pages |
| Config | `DataInitializer` | Seeds 10 students + 10 courses on startup |

---

## 4. Implementation Details

### 4.1 Entity Classes

Entities use JPA annotations for object-relational mapping and Bean Validation for input constraints:

```java
@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor
public class Student {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank @Email(message = "Enter a valid email")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    private String department;

    @Min(2000) @Max(2030)
    private int enrollmentYear;

    @ManyToMany(fetch = FetchType.LAZY,
                cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> courses = new ArrayList<>();
}
```

The `Course` entity uses the inverse side of the relationship:

```java
@ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
@JsonIgnore
private List<Student> students = new ArrayList<>();
```

**Key decisions:**
- `FetchType.LAZY` avoids loading the other side unless explicitly needed.
- `unique = true` on email enforced at both DB level and service layer.
- `@JsonIgnore` on the inverse side prevents infinite recursion.

### 4.2 Repository Layer

Both repositories extend `JpaRepository<Entity, Long>`, gaining built-in CRUD and pagination.

**Custom JPQL queries in `StudentRepository`:**

```java
// INNER JOIN — returns only students who have at least one enrolled course
@Query("SELECT DISTINCT s FROM Student s JOIN FETCH s.courses c ORDER BY s.name")
List<Student> findStudentsWithEnrollments();

// Constructor DTO expression — flat inner-join result for the enrollment report page
@Query("SELECT new com.bits.studentcourse.dto.EnrollmentDTO(" +
       "s.id, s.name, s.email, s.department, s.enrollmentYear, " +
       "c.id, c.title, c.instructor, c.credits, c.duration) " +
       "FROM Student s JOIN s.courses c ORDER BY s.name, c.title")
List<EnrollmentDTO> findAllEnrollmentsAsDTO();
```

**Custom query in `CourseRepository`:**

```java
// INNER JOIN — returns only courses that have at least one student enrolled
@Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.students s ORDER BY c.title")
List<Course> findCoursesWithEnrolledStudents();
```

`JOIN FETCH` eagerly loads the collection in a single SQL query, preventing the N+1 problem. `DISTINCT` removes duplicate rows from the join result.

### 4.3 Service Layer

The service layer handles all business logic and sits between the controller and repository. Both services are annotated with `@Transactional` at class level; read-only methods override with `@Transactional(readOnly = true)`.

**Key logic in `StudentServiceImpl` — duplicate email guard:**

```java
@Override
public Student saveStudent(Student student) {
    studentRepository.findByEmail(student.getEmail()).ifPresent(existing -> {
        throw new DataIntegrityViolationException(
            "A student with email '" + student.getEmail() + "' already exists.");
    });
    return studentRepository.save(student);
}

@Override
public Student updateStudent(Student student) {
    Optional<Student> existing = studentRepository.findByEmail(student.getEmail());
    // Only block if the email belongs to a DIFFERENT student record
    if (existing.isPresent() && !existing.get().getId().equals(student.getId())) {
        throw new DataIntegrityViolationException(
            "Another student already uses email '" + student.getEmail() + "'.");
    }
    return studentRepository.save(student);
}
```

### 4.4 Controller Layer

Controllers handle HTTP routes and bind form data to model objects using `@ModelAttribute`.

**Create flow — `StudentController`:**

```java
// Show blank form
@GetMapping("/new")
public String showCreateForm(Model model) {
    model.addAttribute("student", new Student());
    model.addAttribute("allCourses", courseService.getAllCourses());
    model.addAttribute("formAction", "/students/save");
    return "students/form";
}

// Handle form submission
@PostMapping("/save")
public String saveStudent(@Valid @ModelAttribute("student") Student student,
                          BindingResult bindingResult, Model model,
                          RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("allCourses", courseService.getAllCourses());
        model.addAttribute("formAction", "/students/save");
        return "students/form";                  // show validation errors inline
    }
    try {
        studentService.saveStudent(student);
        redirectAttributes.addFlashAttribute("successMessage", "Student added!");
    } catch (DataIntegrityViolationException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "students/form";                  // show duplicate email error
    }
    return "redirect:/students";                 // PRG pattern
}
```

**Update flow:**

```java
@GetMapping("/edit/{id}")
public String showEditForm(@PathVariable Long id, Model model) {
    studentService.getStudentById(id).ifPresent(student -> {
        model.addAttribute("student", student);
        model.addAttribute("allCourses", courseService.getAllCourses());
        model.addAttribute("formAction", "/students/update");
    });
    return "students/form";
}

@PostMapping("/update")
public String updateStudent(@Valid @ModelAttribute("student") Student student,
                            BindingResult bindingResult, RedirectAttributes ra) {
    if (bindingResult.hasErrors()) return "students/form";
    studentService.updateStudent(student);
    ra.addFlashAttribute("successMessage", "Student updated!");
    return "redirect:/students";
}
```

**Key patterns:**
- **Post-Redirect-Get (PRG):** Redirects after save/update prevents duplicate submissions on page refresh.
- **Flash attributes:** `addFlashAttribute()` persists success messages across the redirect.
- **BindingResult:** Captures `@Valid` errors and displays them inline on the form.

### 4.5 View Layer (JSP)

All pages share a common `header.jsp` (with full CSS + sidebar navigation) and `footer.jsp`.

**Students list — `students/list.jsp`:**

```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success">${successMessage}</div>
</c:if>

<table>
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
</table>
```

**Shared Create/Update form — `students/form.jsp`:**

```jsp
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form action="${formAction}" method="post" modelAttribute="student">
    <form:hidden path="id"/>   <%-- ensures UPDATE on submit when editing --%>

    <form:input path="name" placeholder="Full Name"/>
    <form:errors path="name" cssClass="field-error"/>

    <form:input path="email" type="email"/>
    <form:errors path="email" cssClass="field-error"/>

    <form:select path="department">
        <form:option value="Computer Science"/>
        <form:option value="Mathematics"/>
    </form:select>

    <%-- Multi-select for course enrollment --%>
    <form:select path="courses" multiple="true"
                 items="${allCourses}" itemValue="id" itemLabel="title"/>

    <button type="submit">
        ${empty student.id ? 'Add Student' : 'Update Student'}
    </button>
</form:form>
```

One JSP handles both Create and Update — the controller passes the correct `formAction` and the hidden `id` ensures JPA does an UPDATE when editing.

**Enrollment join view — `students/enrollments.jsp`:**

```jsp
<c:forEach var="e" items="${enrollments}">
    <tr>
        <td>${e.studentName}</td>
        <td>${e.department}</td>
        <td>${e.courseTitle}</td>
        <td>${e.instructor}</td>
        <td>${e.credits} cr</td>
    </tr>
</c:forEach>
```

### 4.6 Database Seeding

`DataInitializer` implements `CommandLineRunner` and inserts data once on startup:

```java
@Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Override @Transactional
    public void run(String... args) {
        if (studentRepository.count() > 0) return; // skip if already seeded

        Course c1 = courseRepository.save(new Course(null, "Introduction to Programming", 4, "Dr. Alan Turing", "16 weeks", List.of()));
        Course c2 = courseRepository.save(new Course(null, "Data Structures & Algorithms", 4, "Dr. Donald Knuth", "16 weeks", List.of()));
        // ... 8 more courses

        createStudent("Alice Johnson", "alice@bits.edu", "Computer Science", 2022, c1, c2);
        // ... 9 more students
    }
}
```

**10 Courses seeded:** Introduction to Programming, Data Structures, Calculus I, Linear Algebra, Database Systems, Operating Systems, Computer Networks, Web Development, Machine Learning, Software Engineering.

**10 Students seeded:** Alice Johnson, Bob Smith, Carol Davis, David Wilson, Emma Brown, Frank Miller, Grace Lee, Henry Taylor, Iris Martinez, Jack Anderson — each enrolled in 2–4 courses.

---

## 5. CRUD Operations

### 5.1 Create

1. User navigates to `/students/new` → Spring renders blank form.
2. User fills the form and clicks **Add Student**.
3. Form POSTs to `/students/save`.
4. `@Valid` triggers Bean Validation — errors shown inline if any.
5. Service checks for duplicate email — error shown if conflict.
6. On success → redirect to `/students` with flash message.

### 5.2 Read

| URL | Controller Method | View |
|---|---|---|
| `/students` | `listStudents()` | `students/list.jsp` — all students |
| `/students/enrollments` | `showEnrollments()` | `students/enrollments.jsp` — inner join |
| `/courses` | `listCourses()` | `courses/list.jsp` — all courses |
| `/courses/enrolled` | `showEnrolledCourses()` | `courses/enrolled.jsp` — inner join |

### 5.3 Update

1. User clicks **Edit** → `GET /students/edit/{id}`.
2. Controller fetches record by ID and pre-fills the form.
3. User edits and submits → `POST /students/update`.
4. Hidden `id` field ensures JPA performs an UPDATE not INSERT.
5. On success → redirect to list with flash message.

---

## 6. Unit Testing

### 6.1 Test Strategy

| Test Class | Type | Framework | What is Tested |
|---|---|---|---|
| `StudentServiceImplTest` | Unit | JUnit 5 + Mockito | Business logic, duplicate email guard |
| `CourseServiceImplTest` | Unit | JUnit 5 + Mockito | CRUD delegation to repository |
| `StudentRepositoryTest` | Integration | @DataJpaTest + H2 | Custom JPQL inner-join query, DTO projection |
| `CourseRepositoryTest` | Integration | @DataJpaTest + H2 | Custom JPQL query, derived queries |

### 6.2 Service Layer Tests (Mockito)

```java
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock StudentRepository studentRepository;
    @InjectMocks StudentServiceImpl studentService;

    @Test
    @DisplayName("saveStudent() throws exception on duplicate email")
    void saveStudent_duplicateEmail_throwsException() {
        when(studentRepository.findByEmail("alice@bits.edu"))
            .thenReturn(Optional.of(mockStudent));

        assertThatThrownBy(() -> studentService.saveStudent(mockStudent))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessageContaining("alice@bits.edu");

        verify(studentRepository, never()).save(any()); // save must NOT be called
    }

    @Test
    @DisplayName("saveStudent() succeeds when email is unique")
    void saveStudent_success() {
        when(studentRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(studentRepository.save(any())).thenReturn(mockStudent);

        Student result = studentService.saveStudent(mockStudent);
        assertThat(result.getName()).isEqualTo("Alice Johnson");
    }
}
```

### 6.3 Repository Layer Tests (@DataJpaTest)

```java
@DataJpaTest
class StudentRepositoryTest {

    @Test
    @DisplayName("INNER JOIN returns only students with at least one enrolled course")
    void findStudentsWithEnrollments_returnsEnrolledOnly() {
        // Alice has a course; Bob has none
        studentRepository.save(bobWithNoCourses);

        List<Student> result = studentRepository.findStudentsWithEnrollments();

        assertThat(result).hasSize(1);                           // Bob excluded
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
        assertThat(result.get(0).getCourses()).isNotEmpty();
    }

    @Test
    @DisplayName("findAllEnrollmentsAsDTO() returns correct flat DTO from inner join")
    void findAllEnrollmentsAsDTO_returnsCorrectDTO() {
        List<EnrollmentDTO> dtos = studentRepository.findAllEnrollmentsAsDTO();

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getStudentName()).isEqualTo("Alice Johnson");
        assertThat(dtos.get(0).getCourseTitle()).isEqualTo("Data Structures");
    }
}
```

---

## 7. How to Run

```bash
# Clone the repository
git clone https://github.com/RajPrakash681/Student-Course-Management-App.git
cd Student-Course-Management-App

# Run the application (must use spring-boot:run for JSP support)
mvn spring-boot:run

# Open in browser
http://localhost:8080/students

# H2 Console (optional — to inspect the database)
http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:studentcoursedb | Username: sa | Password: (blank)

# Run all tests
mvn test
```

---

## 8. Screenshots

**Screenshot 1 — Students List (`/students`)**
> _[Paste screenshot here]_

**Screenshot 2 — Add Student Form (`/students/new`)**
> _[Paste screenshot here]_

**Screenshot 3 — Validation Errors (submit empty form)**
> _[Paste screenshot here]_

**Screenshot 4 — Duplicate Email Error**
> _[Paste screenshot here]_

**Screenshot 5 — Edit Student Form (`/students/edit/1`)**
> _[Paste screenshot here]_

**Screenshot 6 — Enrollment Join View (`/students/enrollments`)**
> _[Paste screenshot here]_

**Screenshot 7 — Courses List (`/courses`)**
> _[Paste screenshot here]_

**Screenshot 8 — Test Results (`mvn test`)**
> _[Paste terminal screenshot here]_

---

## 9. Challenges Faced and Solutions

### Challenge 1: JSP Not Working with Spring Boot JAR
**Problem:** Spring Boot executable JARs do not support JSPs at runtime (the servlet container can't find `.jsp` files inside a nested JAR).

**Solution:** Used `mvn spring-boot:run` which runs the app in exploded mode. Added `tomcat-embed-jasper` as a regular compile dependency and configured the view resolver:
```properties
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
```

---

### Challenge 2: JSTL URI Changed in Jakarta EE 10
**Problem:** The classic JSTL URI `http://java.sun.com/jsp/jstl/core` throws a `JasperException` in Spring Boot 3.x (Jakarta EE 10).

**Solution:** Updated the taglib declaration and Maven dependency:
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
```
```xml
<groupId>org.glassfish.web</groupId>
<artifactId>jakarta.servlet.jsp.jstl</artifactId>
<version>3.0.1</version>
```

---

### Challenge 3: N+1 Query Problem with Lazy ManyToMany
**Problem:** Accessing `student.getCourses()` inside a JSP loop triggered one SQL query per student.

**Solution:** Used `JOIN FETCH` in the JPQL query to load everything in one query:
```java
@Query("SELECT DISTINCT s FROM Student s JOIN FETCH s.courses c ORDER BY s.name")
List<Student> findStudentsWithEnrollments();
```

---

### Challenge 4: One Form for Both Create and Update
**Problem:** Maintaining two identical JSP forms causes code duplication.

**Solution:** Used a single `form.jsp` — the controller passes `formAction` dynamically, and `<form:hidden path="id"/>` submits the ID during updates so JPA knows to UPDATE instead of INSERT.

---

### Challenge 5: Success Message Lost After Redirect
**Problem:** Flash messages added to `Model` are lost after a redirect.

**Solution:** Used `RedirectAttributes.addFlashAttribute()` which stores the message in the session for exactly one request:
```java
redirectAttributes.addFlashAttribute("successMessage", "Student added!");
return "redirect:/students";
```

---

## 10. Conclusion

This application successfully demonstrates a complete Spring Boot MVC project implementing:
- **JPA entities** with `@ManyToMany` relationship and Bean Validation
- **Layered architecture** following SOLID principles and dependency injection
- **Custom JPQL queries** including inner joins and DTO projections
- **Spring MVC patterns** — PRG, flash messages, shared forms, `@Valid` + `BindingResult`
- **JSP + JSTL views** with a modern dark-themed UI
- **Unit and integration tests** covering service business logic and repository queries

**GitHub Repository:** https://github.com/RajPrakash681/Student-Course-Management-App

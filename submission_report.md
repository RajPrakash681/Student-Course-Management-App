# Student–Course Management Application
## Spring Boot Assignment Report

**Student Name:** [Your Name]
**Registration No.:** [Your Reg. No.]
**GitHub:** https://github.com/RajPrakash681/Student-Course-Management-App

---

## 1. Entity Relationship Design

We chose **Student** and **Course** as our two entities. The relationship between them is **Many-to-Many** — a student can enroll in multiple courses and a course can have multiple students enrolled.

### Student Entity

| Field | Type | Notes |
|---|---|---|
| id | Long | Primary key, auto-generated |
| name | String | Required, 2–100 chars |
| email | String | Required, unique, valid email format |
| department | String | Required |
| enrollmentYear | int | 2000–2030 |

### Course Entity

| Field | Type | Notes |
|---|---|---|
| id | Long | Primary key, auto-generated |
| title | String | Required, 2–150 chars |
| credits | int | 1–6 |
| instructor | String | Required |
| duration | String | e.g., "16 weeks" |

### Relationship

The `Student` entity owns the relationship through a join table called `student_course`, which holds `student_id` and `course_id` as foreign keys. The `Course` entity uses `@ManyToMany(mappedBy = "courses")` on the inverse side.

A `CommandLineRunner` bean (`DataInitializer`) seeds the database on startup with **10 students** and **10 courses**, each student enrolled in 2–4 courses.

---

## 2. Implementation Details

### 2.1 Create Operation

Users submit a form to add a new student or course. The form is validated using Bean Validation (`@Valid`) and duplicate emails are caught at the service layer before reaching the database.

**Controller — handling form submission:**
```java
@PostMapping("/save")
public String saveStudent(@Valid @ModelAttribute("student") Student student,
                          BindingResult bindingResult, Model model,
                          RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("allCourses", courseService.getAllCourses());
        return "students/form";
    }
    try {
        studentService.saveStudent(student);
        redirectAttributes.addFlashAttribute("successMessage", "Student added!");
    } catch (DataIntegrityViolationException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "students/form";
    }
    return "redirect:/students";
}
```

**Service — duplicate email check:**
```java
public Student saveStudent(Student student) {
    studentRepository.findByEmail(student.getEmail()).ifPresent(e -> {
        throw new DataIntegrityViolationException(
            "Email '" + student.getEmail() + "' already exists.");
    });
    return studentRepository.save(student);
}
```

**JSP form (shared for create and update):**
```jsp
<form:form action="${formAction}" method="post" modelAttribute="student">
    <form:hidden path="id"/>
    <form:input path="name" placeholder="Full Name"/>
    <form:errors path="name" cssClass="field-error"/>
    <form:input path="email" type="email"/>
    <form:errors path="email" cssClass="field-error"/>
    <form:select path="courses" multiple="true"
                 items="${allCourses}" itemValue="id" itemLabel="title"/>
    <button type="submit">
        ${empty student.id ? 'Add Student' : 'Update Student'}
    </button>
</form:form>
```

> **Screenshot 1 — Add Student Form**
> *[Paste screenshot of /students/new here]*

> **Screenshot 2 — Validation Error**
> *[Paste screenshot showing inline field errors here]*

> **Screenshot 3 — Duplicate Email Error**
> *[Paste screenshot showing the error alert here]*

---

### 2.2 Read Operation

Two types of read views are implemented — a simple list and an inner-join enrollment view.

**Controller — listing students:**
```java
@GetMapping
public String listStudents(Model model) {
    model.addAttribute("students", studentService.getAllStudents());
    return "students/list";
}

@GetMapping("/enrollments")
public String showEnrollments(Model model) {
    model.addAttribute("enrollments", studentService.getAllEnrollments());
    return "students/enrollments";
}
```

**Custom JPQL inner-join query in `StudentRepository`:**
```java
// Returns flat Student+Course pairs — only students with at least one course
@Query("SELECT new com.bits.studentcourse.dto.EnrollmentDTO(" +
       "s.id, s.name, s.email, s.department, s.enrollmentYear, " +
       "c.id, c.title, c.instructor, c.credits, c.duration) " +
       "FROM Student s JOIN s.courses c ORDER BY s.name, c.title")
List<EnrollmentDTO> findAllEnrollmentsAsDTO();
```

**JSP list view:**
```jsp
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

> **Screenshot 4 — Students List**
> *[Paste screenshot of /students showing the 10 seeded students here]*

> **Screenshot 5 — Enrollment Join View**
> *[Paste screenshot of /students/enrollments showing the inner join table here]*

> **Screenshot 6 — Courses List**
> *[Paste screenshot of /courses here]*

---

### 2.3 Update Operation

The same JSP form used for Create is reused for Update. The controller pre-fills the form with existing data and a hidden `id` field ensures JPA performs an UPDATE instead of INSERT.

**Controller — pre-filling and saving update:**
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
                            BindingResult result, RedirectAttributes ra) {
    if (result.hasErrors()) return "students/form";
    studentService.updateStudent(student);
    ra.addFlashAttribute("successMessage", "Student updated!");
    return "redirect:/students";
}
```

> **Screenshot 7 — Edit Student Form**
> *[Paste screenshot of /students/edit/1 showing pre-filled form here]*

---

### 2.4 Unit Tests

**Service test (Mockito) — duplicate email guard:**
```java
@Test
void saveStudent_duplicateEmail_throwsException() {
    when(studentRepository.findByEmail("alice@bits.edu"))
        .thenReturn(Optional.of(mockStudent));

    assertThatThrownBy(() -> studentService.saveStudent(mockStudent))
        .isInstanceOf(DataIntegrityViolationException.class);

    verify(studentRepository, never()).save(any());
}
```

**Repository test (@DataJpaTest) — inner join:**
```java
@Test
void findStudentsWithEnrollments_onlyReturnsEnrolledStudents() {
    List<Student> result = studentRepository.findStudentsWithEnrollments();
    assertThat(result).hasSize(1);  // Bob with no courses is excluded
    assertThat(result.get(0).getCourses()).isNotEmpty();
}
```

> **Screenshot 8 — Test Results**
> *[Paste terminal screenshot of `mvn test` passing here]*

---

## 3. Challenges Faced

**Challenge 1 — JSP not rendering with Spring Boot**
Spring Boot executable JARs don't serve JSPs. Fixed by using `mvn spring-boot:run` (exploded mode) and configuring the view resolver in `application.properties`:
```properties
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
```

**Challenge 2 — JSTL URI changed in Jakarta EE 10**
The old `http://java.sun.com/jsp/jstl/core` URI causes a `JasperException` in Spring Boot 3.x. Fixed by switching to the new Jakarta namespace: `<%@ taglib prefix="c" uri="jakarta.tags.core" %>`.

**Challenge 3 — N+1 query with lazy ManyToMany**
Accessing `student.getCourses()` inside a JSP loop fired one SQL per student. Fixed with `JOIN FETCH` in the JPQL query which loads everything in one query.

**Challenge 4 — Success message lost after redirect**
Model attributes don't survive a redirect. Fixed using `RedirectAttributes.addFlashAttribute()` which stores the message in the session for exactly one request.

**Challenge 5 — Shared form for Create and Update**
Avoided duplicating JSP form code by using a single `form.jsp`. The controller passes the correct `formAction` and a `<form:hidden path="id"/>` field handles the difference between create and update transparently.

---

## 4. GitHub URL

**https://github.com/RajPrakash681/Student-Course-Management-App**

Run locally:
```bash
git clone https://github.com/RajPrakash681/Student-Course-Management-App.git
cd Student-Course-Management-App
mvn spring-boot:run
# Open: http://localhost:8080/students
```

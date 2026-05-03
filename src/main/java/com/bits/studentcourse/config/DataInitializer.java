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

/**
 * Seeds 10 Students and 10 Courses with realistic enrollment data on startup.
 */
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

        // ── Create 10 Courses ──────────────────────────────────────────
        Course c1  = save(new Course(null, "Introduction to Programming",  4, "Dr. Alan Turing",     "16 weeks", List.of()));
        Course c2  = save(new Course(null, "Data Structures & Algorithms", 4, "Dr. Donald Knuth",    "16 weeks", List.of()));
        Course c3  = save(new Course(null, "Calculus I",                   3, "Dr. Isaac Newton",    "16 weeks", List.of()));
        Course c4  = save(new Course(null, "Linear Algebra",               3, "Dr. Carl Gauss",      "14 weeks", List.of()));
        Course c5  = save(new Course(null, "Database Systems",             4, "Dr. Edgar Codd",      "16 weeks", List.of()));
        Course c6  = save(new Course(null, "Operating Systems",            4, "Dr. Linus Torvalds",  "16 weeks", List.of()));
        Course c7  = save(new Course(null, "Computer Networks",            3, "Dr. Vint Cerf",       "14 weeks", List.of()));
        Course c8  = save(new Course(null, "Web Development",              3, "Dr. Tim Berners-Lee", "12 weeks", List.of()));
        Course c9  = save(new Course(null, "Machine Learning",             4, "Dr. Andrew Ng",       "16 weeks", List.of()));
        Course c10 = save(new Course(null, "Software Engineering",         3, "Dr. Fred Brooks",     "14 weeks", List.of()));

        log.info("Seeded 10 courses.");

        // ── Create 10 Students with course enrollments ─────────────────
        createStudent("Alice Johnson",   "alice.johnson@bits.edu",   "Computer Science", 2022, c1, c2, c5);
        createStudent("Bob Smith",       "bob.smith@bits.edu",       "Mathematics",      2021, c3, c4, c9);
        createStudent("Carol Davis",     "carol.davis@bits.edu",     "Physics",          2023, c3, c4, c6);
        createStudent("David Wilson",    "david.wilson@bits.edu",    "Computer Science", 2022, c1, c5, c10);
        createStudent("Emma Brown",      "emma.brown@bits.edu",      "Computer Science", 2021, c2, c7, c8);
        createStudent("Frank Miller",    "frank.miller@bits.edu",    "Engineering",      2023, c6, c7, c10);
        createStudent("Grace Lee",       "grace.lee@bits.edu",       "Biology",          2022, c3, c9);
        createStudent("Henry Taylor",    "henry.taylor@bits.edu",    "Physics",          2021, c1, c4, c6);
        createStudent("Iris Martinez",   "iris.martinez@bits.edu",   "Mathematics",      2023, c3, c8, c9);
        createStudent("Jack Anderson",   "jack.anderson@bits.edu",   "Computer Science", 2022, c1, c2, c5, c7);

        log.info("Seeded 10 students with enrollments.");
    }

    private Course save(Course course) {
        return courseRepository.save(course);
    }

    private void createStudent(String name, String email, String dept, int year, Course... courses) {
        Student s = new Student();
        s.setName(name);
        s.setEmail(email);
        s.setDepartment(dept);
        s.setEnrollmentYear(year);
        for (Course c : courses) {
            s.getCourses().add(c);
        }
        studentRepository.save(s);
    }
}

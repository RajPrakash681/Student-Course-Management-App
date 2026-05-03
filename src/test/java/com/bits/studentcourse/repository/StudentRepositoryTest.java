package com.bits.studentcourse.repository;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("StudentRepository Integration Tests")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

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
    @DisplayName("findByEmail() should return empty for non-existent email")
    void findByEmail_notFound() {
        assertThat(studentRepository.findByEmail("nobody@test.com")).isEmpty();
    }

    @Test
    @DisplayName("findByDepartment() should return students in specified department")
    void findByDepartment() {
        List<Student> result = studentRepository.findByDepartment("Computer Science");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("findByEnrollmentYear() should return students enrolled in given year")
    void findByEnrollmentYear() {
        List<Student> result = studentRepository.findByEnrollmentYear(2022);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEnrollmentYear()).isEqualTo(2022);
    }

    @Test
    @DisplayName("findStudentsWithEnrollments() INNER JOIN returns only enrolled students")
    void findStudentsWithEnrollments_returnsEnrolledOnly() {
        // Add a student with no courses
        Student noCoursesStudent = new Student();
        noCoursesStudent.setName("Bob NoEnroll");
        noCoursesStudent.setEmail("bob@test.com");
        noCoursesStudent.setDepartment("Physics");
        noCoursesStudent.setEnrollmentYear(2023);
        studentRepository.save(noCoursesStudent);

        List<Student> result = studentRepository.findStudentsWithEnrollments();

        // Only Alice (who has a course) should be returned
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
        assertThat(result.get(0).getCourses()).isNotEmpty();
    }

    @Test
    @DisplayName("findAllEnrollmentsAsDTO() should return flat DTO with joined student-course data")
    void findAllEnrollmentsAsDTO_returnsCorrectDTO() {
        List<EnrollmentDTO> dtos = studentRepository.findAllEnrollmentsAsDTO();

        assertThat(dtos).hasSize(1);
        EnrollmentDTO dto = dtos.get(0);
        assertThat(dto.getStudentName()).isEqualTo("Alice Johnson");
        assertThat(dto.getCourseTitle()).isEqualTo("Data Structures");
        assertThat(dto.getCredits()).isEqualTo(4);
        assertThat(dto.getInstructor()).isEqualTo("Dr. Knuth");
    }
}

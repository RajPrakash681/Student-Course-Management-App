package com.bits.studentcourse.repository;

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
@DisplayName("CourseRepository Integration Tests")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

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
    @DisplayName("findById() should return course when exists")
    void findById_found() {
        Optional<Course> result = courseRepository.findById(savedCourse.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Machine Learning");
    }

    @Test
    @DisplayName("findByInstructor() should return courses by instructor")
    void findByInstructor() {
        List<Course> result = courseRepository.findByInstructor("Dr. Andrew Ng");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Machine Learning");
    }

    @Test
    @DisplayName("findByCredits() should return courses with exact credit count")
    void findByCredits() {
        List<Course> result = courseRepository.findByCredits(4);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCredits()).isEqualTo(4);
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase() should do case-insensitive search")
    void findByTitleContaining() {
        List<Course> result = courseRepository.findByTitleContainingIgnoreCase("machine");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Machine Learning");
    }

    @Test
    @DisplayName("findCoursesWithEnrolledStudents() INNER JOIN returns only courses with students")
    void findCoursesWithEnrolledStudents_innerJoin() {
        // Add a student enrolled in Machine Learning
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
        assertThat(result.get(0).getStudents()).hasSize(1);
    }

    @Test
    @DisplayName("findByMinCredits() should return courses with credits >= threshold")
    void findByMinCredits() {
        List<Course> result = courseRepository.findByMinCredits(4);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCredits()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("save() should persist a new course and auto-generate ID")
    void save_persistsAndGeneratesId() {
        Course newCourse = new Course(null, "Web Dev", 3, "Dr. Tim", "12 weeks", List.of());
        Course saved = courseRepository.save(newCourse);

        assertThat(saved.getId()).isNotNull();
        assertThat(courseRepository.count()).isEqualTo(3);
    }
}

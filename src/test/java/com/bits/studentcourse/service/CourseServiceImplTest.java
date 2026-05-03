package com.bits.studentcourse.service;

import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

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
    @DisplayName("getAllCourses() returns empty list when no courses")
    void getAllCourses_empty() {
        when(courseRepository.findAll()).thenReturn(List.of());
        assertThat(courseService.getAllCourses()).isEmpty();
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
    @DisplayName("getCourseById() returns empty when not found")
    void getCourseById_notFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(courseService.getCourseById(99L)).isEmpty();
    }

    @Test
    @DisplayName("saveCourse() should persist and return the saved course")
    void saveCourse_success() {
        when(courseRepository.save(any(Course.class))).thenReturn(mockCourse);

        Course result = courseService.saveCourse(mockCourse);

        assertThat(result.getId()).isEqualTo(1L);
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
        verify(courseRepository).save(mockCourse);
    }

    @Test
    @DisplayName("deleteCourse() should call deleteById on repository")
    void deleteCourse_callsRepository() {
        doNothing().when(courseRepository).deleteById(1L);

        courseService.deleteCourse(1L);

        verify(courseRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("getCoursesWithEnrolledStudents() returns courses from repository join query")
    void getCoursesWithEnrolledStudents_returnsList() {
        when(courseRepository.findCoursesWithEnrolledStudents()).thenReturn(List.of(mockCourse));

        List<Course> result = courseService.getCoursesWithEnrolledStudents();

        assertThat(result).hasSize(1);
        verify(courseRepository).findCoursesWithEnrolledStudents();
    }
}

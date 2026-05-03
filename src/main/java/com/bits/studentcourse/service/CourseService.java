package com.bits.studentcourse.service;

import com.bits.studentcourse.entity.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {

    List<Course> getAllCourses();

    Optional<Course> getCourseById(Long id);

    Course saveCourse(Course course);

    Course updateCourse(Course course);

    void deleteCourse(Long id);

    List<Course> getCoursesWithEnrolledStudents();
}

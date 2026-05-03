package com.bits.studentcourse.service;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    List<Student> getAllStudents();

    Optional<Student> getStudentById(Long id);

    Student saveStudent(Student student);

    Student updateStudent(Student student);

    void deleteStudent(Long id);

    List<Student> getStudentsWithEnrollments();

    List<EnrollmentDTO> getAllEnrollments();

    boolean existsByEmail(String email);
}

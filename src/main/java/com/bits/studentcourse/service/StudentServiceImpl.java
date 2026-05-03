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

    /**
     * Saves a new student. Throws DataIntegrityViolationException if email already exists.
     */
    @Override
    public Student saveStudent(Student student) {
        studentRepository.findByEmail(student.getEmail()).ifPresent(existing -> {
            throw new DataIntegrityViolationException(
                    "A student with email '" + student.getEmail() + "' already exists.");
        });
        return studentRepository.save(student);
    }

    /**
     * Updates an existing student. Email uniqueness is checked against other records.
     */
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

package com.bits.studentcourse.service;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.entity.Student;
import com.bits.studentcourse.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentServiceImpl Unit Tests")
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setName("Alice Johnson");
        mockStudent.setEmail("alice@bits.edu");
        mockStudent.setDepartment("Computer Science");
        mockStudent.setEnrollmentYear(2022);
    }

    // ── getAllStudents ─────────────────────────────────────────────────
    @Test
    @DisplayName("getAllStudents() should return all students from repository")
    void getAllStudents_returnsAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(mockStudent));

        List<Student> result = studentService.getAllStudents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice Johnson");
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllStudents() should return empty list when no students exist")
    void getAllStudents_returnsEmptyList() {
        when(studentRepository.findAll()).thenReturn(List.of());

        List<Student> result = studentService.getAllStudents();

        assertThat(result).isEmpty();
    }

    // ── getStudentById ─────────────────────────────────────────────────
    @Test
    @DisplayName("getStudentById() should return student when found")
    void getStudentById_found() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        Optional<Student> result = studentService.getStudentById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStudentById() should return empty when not found")
    void getStudentById_notFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Student> result = studentService.getStudentById(99L);

        assertThat(result).isEmpty();
    }

    // ── saveStudent ────────────────────────────────────────────────────
    @Test
    @DisplayName("saveStudent() should save and return student when email is unique")
    void saveStudent_success() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        Student result = studentService.saveStudent(mockStudent);

        assertThat(result.getName()).isEqualTo("Alice Johnson");
        verify(studentRepository).save(mockStudent);
    }

    @Test
    @DisplayName("saveStudent() should throw DataIntegrityViolationException on duplicate email")
    void saveStudent_duplicateEmail_throwsException() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(mockStudent));

        assertThatThrownBy(() -> studentService.saveStudent(mockStudent))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("alice@bits.edu");

        verify(studentRepository, never()).save(any());
    }

    // ── updateStudent ──────────────────────────────────────────────────
    @Test
    @DisplayName("updateStudent() should update when email is unchanged")
    void updateStudent_sameEmail_success() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        mockStudent.setName("Alice Updated");
        Student result = studentService.updateStudent(mockStudent);

        assertThat(result).isNotNull();
        verify(studentRepository).save(mockStudent);
    }

    @Test
    @DisplayName("updateStudent() should throw when email is used by a different student")
    void updateStudent_emailConflict_throwsException() {
        Student other = new Student();
        other.setId(2L);
        other.setEmail("alice@bits.edu");

        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(other));

        mockStudent.setId(1L); // different ID
        assertThatThrownBy(() -> studentService.updateStudent(mockStudent))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── getAllEnrollments ──────────────────────────────────────────────
    @Test
    @DisplayName("getAllEnrollments() should return flat DTO list from repository")
    void getAllEnrollments_returnsDTOList() {
        EnrollmentDTO dto = new EnrollmentDTO(1L, "Alice", "alice@bits.edu",
                "CS", 2022, 1L, "Data Structures", "Dr. Knuth", 4, "16 weeks");
        when(studentRepository.findAllEnrollmentsAsDTO()).thenReturn(List.of(dto));

        List<EnrollmentDTO> result = studentService.getAllEnrollments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseTitle()).isEqualTo("Data Structures");
    }

    // ── existsByEmail ──────────────────────────────────────────────────
    @Test
    @DisplayName("existsByEmail() should return true when email exists")
    void existsByEmail_true() {
        when(studentRepository.findByEmail("alice@bits.edu")).thenReturn(Optional.of(mockStudent));
        assertThat(studentService.existsByEmail("alice@bits.edu")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail() should return false when email does not exist")
    void existsByEmail_false() {
        when(studentRepository.findByEmail("nobody@bits.edu")).thenReturn(Optional.empty());
        assertThat(studentService.existsByEmail("nobody@bits.edu")).isFalse();
    }
}

package com.bits.studentcourse.repository;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Derived query — find by unique email
    Optional<Student> findByEmail(String email);

    // Derived query — filter by department
    List<Student> findByDepartment(String department);

    // Derived query — filter by enrollment year
    List<Student> findByEnrollmentYear(int year);

    /**
     * Custom JPQL — INNER JOIN: fetches only students who have at least one course.
     * JOIN FETCH avoids N+1 by eagerly loading courses in one query.
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN FETCH s.courses c ORDER BY s.name")
    List<Student> findStudentsWithEnrollments();

    /**
     * Custom JPQL — returns a flat DTO result of the inner join between
     * Student and Course, used for the Enrollment report page.
     */
    @Query("SELECT new com.bits.studentcourse.dto.EnrollmentDTO(" +
           "s.id, s.name, s.email, s.department, s.enrollmentYear, " +
           "c.id, c.title, c.instructor, c.credits, c.duration) " +
           "FROM Student s JOIN s.courses c ORDER BY s.name, c.title")
    List<EnrollmentDTO> findAllEnrollmentsAsDTO();

    /**
     * Custom JPQL — students enrolled in courses with at least minCredits.
     */
    @Query("SELECT DISTINCT s FROM Student s JOIN s.courses c WHERE c.credits >= :minCredits")
    List<Student> findStudentsEnrolledInHighCreditCourses(@Param("minCredits") int minCredits);
}

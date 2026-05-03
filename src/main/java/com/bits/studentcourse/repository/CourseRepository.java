package com.bits.studentcourse.repository;

import com.bits.studentcourse.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Derived query — find by instructor name
    List<Course> findByInstructor(String instructor);

    // Derived query — find by exact credits
    List<Course> findByCredits(int credits);

    // Derived query — find by title containing keyword (case-insensitive)
    List<Course> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Custom JPQL — INNER JOIN: fetches only courses that have at least one enrolled student.
     * JOIN FETCH avoids N+1 issue.
     */
    @Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.students s ORDER BY c.title")
    List<Course> findCoursesWithEnrolledStudents();

    /**
     * Custom JPQL — courses with credits at or above a minimum threshold.
     */
    @Query("SELECT c FROM Course c WHERE c.credits >= :minCredits ORDER BY c.credits DESC")
    List<Course> findByMinCredits(@Param("minCredits") int minCredits);
}

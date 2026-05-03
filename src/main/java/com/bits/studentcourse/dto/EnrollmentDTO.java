package com.bits.studentcourse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the inner-join result between Student and Course.
 * Used by the custom JPQL query in StudentRepository.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String department;
    private int enrollmentYear;
    private Long courseId;
    private String courseTitle;
    private String instructor;
    private int credits;
    private String duration;
}

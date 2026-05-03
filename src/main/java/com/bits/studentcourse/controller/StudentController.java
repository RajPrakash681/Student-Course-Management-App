package com.bits.studentcourse.controller;

import com.bits.studentcourse.dto.EnrollmentDTO;
import com.bits.studentcourse.entity.Student;
import com.bits.studentcourse.service.CourseService;
import com.bits.studentcourse.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;

    // ── READ: List all students ────────────────────────────────────────
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("pageTitle", "All Students");
        return "students/list";
    }

    // ── CREATE: Show blank form ────────────────────────────────────────
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("allCourses", courseService.getAllCourses());
        model.addAttribute("pageTitle", "Add New Student");
        model.addAttribute("formAction", "/students/save");
        return "students/form";
    }

    // ── CREATE: Handle form submission ─────────────────────────────────
    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Add New Student");
            model.addAttribute("formAction", "/students/save");
            return "students/form";
        }
        try {
            studentService.saveStudent(student);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student '" + student.getName() + "' added successfully!");
        } catch (DataIntegrityViolationException e) {
            log.warn("Integrity violation on save student: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Add New Student");
            model.addAttribute("formAction", "/students/save");
            return "students/form";
        }
        return "redirect:/students";
    }

    // ── UPDATE: Show pre-filled form ───────────────────────────────────
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return studentService.getStudentById(id).map(student -> {
            model.addAttribute("student", student);
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Edit Student");
            model.addAttribute("formAction", "/students/update");
            return "students/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Student with ID " + id + " not found.");
            return "redirect:/students";
        });
    }

    // ── UPDATE: Handle update submission ──────────────────────────────
    @PostMapping("/update")
    public String updateStudent(@Valid @ModelAttribute("student") Student student,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Edit Student");
            model.addAttribute("formAction", "/students/update");
            return "students/form";
        }
        try {
            studentService.updateStudent(student);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student '" + student.getName() + "' updated successfully!");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("pageTitle", "Edit Student");
            model.addAttribute("formAction", "/students/update");
            return "students/form";
        }
        return "redirect:/students";
    }

    // ── READ: Enrollment join view ─────────────────────────────────────
    @GetMapping("/enrollments")
    public String showEnrollments(Model model) {
        List<EnrollmentDTO> enrollments = studentService.getAllEnrollments();
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("pageTitle", "Student Enrollments (Inner Join)");
        return "students/enrollments";
    }
}

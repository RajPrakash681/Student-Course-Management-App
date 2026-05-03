package com.bits.studentcourse.controller;

import com.bits.studentcourse.entity.Course;
import com.bits.studentcourse.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    // ── READ: List all courses ─────────────────────────────────────────
    @GetMapping
    public String listCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("pageTitle", "All Courses");
        return "courses/list";
    }

    // ── CREATE: Show blank form ────────────────────────────────────────
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("pageTitle", "Add New Course");
        model.addAttribute("formAction", "/courses/save");
        return "courses/form";
    }

    // ── CREATE: Handle form submission ─────────────────────────────────
    @PostMapping("/save")
    public String saveCourse(@Valid @ModelAttribute("course") Course course,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Course");
            model.addAttribute("formAction", "/courses/save");
            return "courses/form";
        }
        courseService.saveCourse(course);
        redirectAttributes.addFlashAttribute("successMessage",
                "Course '" + course.getTitle() + "' added successfully!");
        return "redirect:/courses";
    }

    // ── UPDATE: Show pre-filled form ───────────────────────────────────
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return courseService.getCourseById(id).map(course -> {
            model.addAttribute("course", course);
            model.addAttribute("pageTitle", "Edit Course");
            model.addAttribute("formAction", "/courses/update");
            return "courses/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMessage", "Course with ID " + id + " not found.");
            return "redirect:/courses";
        });
    }

    // ── UPDATE: Handle update submission ──────────────────────────────
    @PostMapping("/update")
    public String updateCourse(@Valid @ModelAttribute("course") Course course,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Course");
            model.addAttribute("formAction", "/courses/update");
            return "courses/form";
        }
        courseService.updateCourse(course);
        redirectAttributes.addFlashAttribute("successMessage",
                "Course '" + course.getTitle() + "' updated successfully!");
        return "redirect:/courses";
    }

    // ── READ: Courses with enrolled students (join result) ────────────
    @GetMapping("/enrolled")
    public String showEnrolledCourses(Model model) {
        model.addAttribute("courses", courseService.getCoursesWithEnrolledStudents());
        model.addAttribute("pageTitle", "Courses With Enrolled Students");
        return "courses/enrolled";
    }
}

package com.example.QuanLyKTX.student;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/** Quản lý sinh viên – Ban quản lý + Nhân viên quản lý (xem SecurityConfig: /student/**). */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("students", studentService.search(q));
        model.addAttribute("q", q == null ? "" : q);
        return "student/student-list";
    }

    @PostMapping("/{id}/lock")
    public String toggleLock(@PathVariable Integer id, @RequestParam(required = false) String q,
                             RedirectAttributes ra) {
        Optional<Boolean> locked = studentService.toggleLock(id);
        if (locked.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy sinh viên");
        } else {
            ra.addFlashAttribute("success", locked.get() ? "Đã khóa tài khoản sinh viên" : "Đã mở khóa tài khoản sinh viên");
        }
        return (q == null || q.isBlank()) ? "redirect:/student" : "redirect:/student?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8);
    }
}

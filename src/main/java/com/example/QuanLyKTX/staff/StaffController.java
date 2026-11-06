package com.example.QuanLyKTX.staff;

import com.example.QuanLyKTX.entity.TaiKhoan;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/** Quản lý nhân viên – CHỈ Ban quản lý (xem SecurityConfig: /staff/**). */
@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String role, Model model) {
        model.addAttribute("staff", staffService.search(q, role));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("role", role == null ? "" : role);
        model.addAttribute("roleOptions", StaffService.ROLE_OPTIONS);
        return "staff/staff-list";
    }

    // ------------------------------------------------------------ THÊM
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new StaffForm());
        return formView(model, true, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") StaffForm form, BindingResult result, Model model,
                         RedirectAttributes ra) {
        if (!result.hasFieldErrors("vaiTro") && !StaffService.isStaffRole(form.getVaiTro())) {
            result.rejectValue("vaiTro", "invalid", "Vai trò không hợp lệ");
        }
        if (!result.hasFieldErrors("tenDangNhap") && staffService.usernameExists(form.getTenDangNhap())) {
            result.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập đã được sử dụng");
        }
        checkPassword(form, result, true);
        if (result.hasErrors()) {
            return formView(model, true, null);
        }
        try {
            staffService.create(form);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập đã được sử dụng");
            return formView(model, true, null);
        }
        ra.addFlashAttribute("success", "Đã thêm nhân viên " + form.getTenDangNhap().trim());
        return "redirect:/staff";
    }

    // ------------------------------------------------------------ SỬA (đổi vai trò / đặt lại mật khẩu)
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<TaiKhoan> opt = staffService.findStaff(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy nhân viên");
            return "redirect:/staff";
        }
        StaffForm form = new StaffForm();
        form.setTenDangNhap(opt.get().getTenDangNhap());
        form.setVaiTro(opt.get().getVaiTro().getTenVaiTro());
        model.addAttribute("form", form);
        return formView(model, false, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute("form") StaffForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        Optional<TaiKhoan> opt = staffService.findStaff(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy nhân viên");
            return "redirect:/staff";
        }
        form.setTenDangNhap(opt.get().getTenDangNhap());   // tên đăng nhập không đổi
        if (!StaffService.isStaffRole(form.getVaiTro())) {
            result.rejectValue("vaiTro", "invalid", "Vai trò không hợp lệ");
        }
        checkPassword(form, result, false);
        if (result.hasErrors()) {
            return formView(model, false, id);
        }
        staffService.update(id, form.getVaiTro(), form.getMatKhau());
        ra.addFlashAttribute("success", "Đã cập nhật nhân viên " + form.getTenDangNhap());
        return "redirect:/staff";
    }

    // ------------------------------------------------------------ KHÓA / MỞ KHÓA
    @PostMapping("/{id}/lock")
    public String toggleLock(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Boolean> locked = staffService.toggleLock(id);
        if (locked.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy nhân viên");
        } else {
            ra.addFlashAttribute("success", locked.get() ? "Đã khóa tài khoản nhân viên" : "Đã mở khóa tài khoản nhân viên");
        }
        return "redirect:/staff";
    }

    // ------------------------------------------------------------ tiện ích
    private String formView(Model model, boolean isNew, Integer id) {
        model.addAttribute("isNew", isNew);
        model.addAttribute("staffId", id);
        model.addAttribute("roleOptions", StaffService.ROLE_OPTIONS);
        return "staff/staff-form";
    }

    /** Thêm mới: bắt buộc mật khẩu. Sửa: để trống = giữ nguyên mật khẩu cũ. */
    private void checkPassword(StaffForm f, BindingResult result, boolean required) {
        String pw = f.getMatKhau();
        boolean empty = pw == null || pw.isBlank();
        if (empty) {
            if (required) result.rejectValue("matKhau", "required", "Vui lòng nhập mật khẩu");
            return;
        }
        if (pw.length() < 6 || pw.length() > 50) {
            result.rejectValue("matKhau", "size", "Mật khẩu từ 6 đến 50 ký tự");
        } else if (!pw.equals(f.getXacNhanMatKhau())) {
            result.rejectValue("xacNhanMatKhau", "mismatch", "Mật khẩu nhập lại không khớp");
        }
    }
}

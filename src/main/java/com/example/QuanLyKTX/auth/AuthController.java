package com.example.QuanLyKTX.auth;

import com.example.QuanLyKTX.config.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

/** Đăng nhập (xử lý bởi Spring Security), đăng ký tài khoản, quên/đặt lại mật khẩu. Đăng xuất: POST /logout. */
@Controller
public class AuthController {

    private static final String SESSION_RESET_USER = "resetUserId";
    private static final String SESSION_RESET_EXPIRES = "resetExpiresAt";
    private static final long RESET_VALID_MILLIS = 10 * 60 * 1000L;   // 10 phút

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    // ---------------------------------------------------------------- ĐĂNG NHẬP
    @GetMapping("/login")
    public String login(@AuthenticationPrincipal CustomUserDetails user) {
        return user != null ? "redirect:/home" : "auth/login";
    }

    // ---------------------------------------------------------------- ĐĂNG KÝ
    @GetMapping("/register")
    public String registerForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) return "redirect:/home";
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form,
                           BindingResult result) {
        if (form.getMatKhau() != null && form.getXacNhanMatKhau() != null
                && !form.getMatKhau().equals(form.getXacNhanMatKhau())) {
            result.rejectValue("xacNhanMatKhau", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!result.hasFieldErrors("tenDangNhap") && accountService.usernameExists(form.getTenDangNhap())) {
            result.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập đã được sử dụng");
        }
        if (!result.hasFieldErrors("maSoSinhVien") && accountService.studentCodeExists(form.getMaSoSinhVien())) {
            result.rejectValue("maSoSinhVien", "duplicate", "Mã sinh viên này đã có tài khoản");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            accountService.registerStudent(form);
        } catch (DataIntegrityViolationException e) {   // trùng do hai người đăng ký cùng lúc
            result.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập hoặc mã sinh viên đã tồn tại");
            return "auth/register";
        }
        return "redirect:/login?registered";
    }

    // ---------------------------------------------------------------- QUÊN MẬT KHẨU
    @GetMapping("/forgot-password")
    public String forgotForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) return "redirect:/home";
        model.addAttribute("form", new ForgotPasswordForm());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@Valid @ModelAttribute("form") ForgotPasswordForm form,
                         BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }
        Optional<Integer> account = accountService.verifyForReset(form.getTenDangNhap(), form.getEmail());
        if (account.isEmpty()) {
            model.addAttribute("notFound", true);
            return "auth/forgot-password";
        }
        // Xác minh xong: cho phép đặt lại mật khẩu trong 10 phút (lưu trong session)
        session.setAttribute(SESSION_RESET_USER, account.get());
        session.setAttribute(SESSION_RESET_EXPIRES, System.currentTimeMillis() + RESET_VALID_MILLIS);
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(HttpSession session, Model model) {
        if (resetUserId(session) == null) return "redirect:/forgot-password?expired";
        model.addAttribute("form", new ResetPasswordForm());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@Valid @ModelAttribute("form") ResetPasswordForm form,
                        BindingResult result, HttpSession session) {
        Integer userId = resetUserId(session);
        if (userId == null) return "redirect:/forgot-password?expired";

        if (form.getMatKhau() != null && form.getXacNhanMatKhau() != null
                && !form.getMatKhau().equals(form.getXacNhanMatKhau())) {
            result.rejectValue("xacNhanMatKhau", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (result.hasErrors()) {
            return "auth/reset-password";
        }
        accountService.resetPassword(userId, form.getMatKhau());
        session.removeAttribute(SESSION_RESET_USER);
        session.removeAttribute(SESSION_RESET_EXPIRES);
        return "redirect:/login?reset";
    }

    /** Mã tài khoản đã được xác minh ở bước quên mật khẩu (còn hạn), hoặc null. */
    private Integer resetUserId(HttpSession session) {
        Object id = session.getAttribute(SESSION_RESET_USER);
        Object exp = session.getAttribute(SESSION_RESET_EXPIRES);
        if (id instanceof Integer userId && exp instanceof Long expiresAt
                && System.currentTimeMillis() < expiresAt) {
            return userId;
        }
        session.removeAttribute(SESSION_RESET_USER);
        session.removeAttribute(SESSION_RESET_EXPIRES);
        return null;
    }

    // ---------------------------------------------------------------- KHÔNG ĐỦ QUYỀN
    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}

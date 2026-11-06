package com.example.QuanLyKTX.account;

import com.example.QuanLyKTX.auth.AccountService;
import com.example.QuanLyKTX.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Trang thông tin tài khoản của người đang đăng nhập (Bình có thể mở rộng: đổi mật khẩu, cập nhật thông tin...). */
@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account")
    public String profile(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("sinhVien", accountService.findStudent(user.getMaTaiKhoan()).orElse(null));
        return "auth/profile";
    }
}

package com.example.QuanLyKTX.auth;

import com.example.QuanLyKTX.common.Roles;
import com.example.QuanLyKTX.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/** Trang chủ: nội dung đổi theo vai trò (mô tả công việc + số liệu nhanh cho quản lý). */
@Controller
public class HomeController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final SinhVienRepository sinhVienRepository;

    public HomeController(TaiKhoanRepository taiKhoanRepository, SinhVienRepository sinhVienRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.sinhVienRepository = sinhVienRepository;
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        String role = user.getVaiTro();
        model.addAttribute("roleIntro", Roles.intro(role));

        if (Roles.BAN_QUAN_LY.equals(role)) {
            model.addAttribute("staffCount", taiKhoanRepository.countByVaiTro_TenVaiTroIn(
                    List.of(Roles.NV_QUAN_LY, Roles.NV_THU_PHI, Roles.NV_BAO_TRI)));
        }
        if (Roles.BAN_QUAN_LY.equals(role) || Roles.NV_QUAN_LY.equals(role)) {
            model.addAttribute("studentCount", sinhVienRepository.count());
        }
        return "home";
    }
}

package com.example.QuanLyKTX.common;

import com.example.QuanLyKTX.config.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Tự động nhét vào MỌI trang: currentUser (tên, vai trò) và menus (menu theo quyền).
 * Các thành viên không cần tự thêm gì vào Controller của mình.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentUser")
    public CurrentUserView currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails u) {
            return new CurrentUserView(u.getHoTen(), u.getUsername(), u.getVaiTro());
        }
        return null;   // chưa đăng nhập
    }

    @ModelAttribute("menus")
    public List<MenuItem> menus(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails u) {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            return MenuBuilder.forRole(u.getVaiTro(), path);
        }
        return List.of();
    }
}

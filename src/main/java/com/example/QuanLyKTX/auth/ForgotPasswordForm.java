package com.example.QuanLyKTX.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordForm {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập email đã đăng ký")
    @Email(message = "Email không đúng định dạng")
    private String email;

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

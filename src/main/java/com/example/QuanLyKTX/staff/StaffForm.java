package com.example.QuanLyKTX.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Dữ liệu form thêm / sửa tài khoản nhân viên. */
public class StaffForm {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Pattern(regexp = "^[A-Za-z0-9_.]{4,50}$", message = "Tên đăng nhập gồm 4-50 ký tự: chữ, số, dấu _ hoặc .")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng chọn vai trò")
    private String vaiTro;

    private String matKhau;
    private String xacNhanMatKhau;

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getXacNhanMatKhau() { return xacNhanMatKhau; }
    public void setXacNhanMatKhau(String xacNhanMatKhau) { this.xacNhanMatKhau = xacNhanMatKhau; }
}

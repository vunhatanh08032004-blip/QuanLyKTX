package com.example.QuanLyKTX.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu form đăng ký tài khoản sinh viên. */
public class RegisterForm {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Vui lòng nhập mã số sinh viên")
    @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "Mã sinh viên gồm 6-20 chữ và số, không dấu cách")
    private String maSoSinhVien;

    @Size(max = 50, message = "Lớp tối đa 50 ký tự")
    private String lop;

    @Size(max = 100, message = "Khoa tối đa 100 ký tự")
    private String khoa;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Pattern(regexp = "^(0\\d{9,10})?$", message = "Số điện thoại phải bắt đầu bằng 0 và có 10-11 số")
    private String soDienThoai;

    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Pattern(regexp = "^[A-Za-z0-9_.]{4,50}$", message = "Tên đăng nhập gồm 4-50 ký tự: chữ, số, dấu _ hoặc .")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, max = 50, message = "Mật khẩu từ 6 đến 50 ký tự")
    private String matKhau;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String xacNhanMatKhau;

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getMaSoSinhVien() { return maSoSinhVien; }
    public void setMaSoSinhVien(String maSoSinhVien) { this.maSoSinhVien = maSoSinhVien; }
    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }
    public String getKhoa() { return khoa; }
    public void setKhoa(String khoa) { this.khoa = khoa; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getXacNhanMatKhau() { return xacNhanMatKhau; }
    public void setXacNhanMatKhau(String xacNhanMatKhau) { this.xacNhanMatKhau = xacNhanMatKhau; }
}

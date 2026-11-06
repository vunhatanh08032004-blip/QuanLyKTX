package com.example.QuanLyKTX.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Thông tin người đăng nhập, lưu trong session. Dùng để hiện tên ở header và dựng menu theo quyền. */
public class CustomUserDetails implements UserDetails {

    private final Integer maTaiKhoan;
    private final String tenDangNhap;
    private final String matKhau;
    private final String vaiTro;     // VD: SINH_VIEN
    private final boolean biKhoa;
    private final String hoTen;      // tên hiển thị (sinh viên: HoTen, nhân viên: tên đăng nhập)

    public CustomUserDetails(Integer maTaiKhoan, String tenDangNhap, String matKhau,
                             String vaiTro, boolean biKhoa, String hoTen) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.biKhoa = biKhoa;
        this.hoTen = hoTen;
    }

    public Integer getMaTaiKhoan() { return maTaiKhoan; }
    public String getVaiTro() { return vaiTro; }
    public String getHoTen() { return hoTen; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro));
    }

    @Override public String getPassword() { return matKhau; }
    @Override public String getUsername() { return tenDangNhap; }
    @Override public boolean isAccountNonLocked() { return !biKhoa; }
}

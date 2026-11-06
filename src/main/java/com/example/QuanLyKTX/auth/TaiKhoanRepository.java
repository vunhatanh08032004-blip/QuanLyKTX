package com.example.QuanLyKTX.auth;

import com.example.QuanLyKTX.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    boolean existsByTenDangNhap(String tenDangNhap);

    /** Danh sách tài khoản theo tập vai trò (dùng cho quản lý nhân viên). */
    List<TaiKhoan> findByVaiTro_TenVaiTroInOrderByTenDangNhap(Collection<String> tenVaiTro);

    long countByVaiTro_TenVaiTroIn(Collection<String> tenVaiTro);
}

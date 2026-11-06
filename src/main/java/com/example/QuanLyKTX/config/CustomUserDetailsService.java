package com.example.QuanLyKTX.config;

import com.example.QuanLyKTX.auth.SinhVienRepository;
import com.example.QuanLyKTX.auth.TaiKhoanRepository;
import com.example.QuanLyKTX.entity.SinhVien;
import com.example.QuanLyKTX.entity.TaiKhoan;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đọc tài khoản từ bảng TaiKhoan để Spring Security xác thực. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final SinhVienRepository sinhVienRepository;

    public CustomUserDetailsService(TaiKhoanRepository taiKhoanRepository,
                                    SinhVienRepository sinhVienRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.sinhVienRepository = sinhVienRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

        String hoTen = sinhVienRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan())
                .map(SinhVien::getHoTen)
                .orElse(tk.getTenDangNhap());

        return new CustomUserDetails(
                tk.getMaTaiKhoan(),
                tk.getTenDangNhap(),
                tk.getMatKhau(),
                tk.getVaiTro().getTenVaiTro(),
                TaiKhoan.KHOA.equals(tk.getTrangThai()),
                hoTen);
    }
}

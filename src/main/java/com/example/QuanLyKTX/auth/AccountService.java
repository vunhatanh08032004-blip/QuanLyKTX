package com.example.QuanLyKTX.auth;

import com.example.QuanLyKTX.common.Roles;
import com.example.QuanLyKTX.entity.SinhVien;
import com.example.QuanLyKTX.entity.TaiKhoan;
import com.example.QuanLyKTX.entity.VaiTro;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final SinhVienRepository sinhVienRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(TaiKhoanRepository taiKhoanRepository,
                          VaiTroRepository vaiTroRepository,
                          SinhVienRepository sinhVienRepository,
                          PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return taiKhoanRepository.existsByTenDangNhap(username.trim());
    }

    public boolean studentCodeExists(String mssv) {
        return sinhVienRepository.existsByMaSoSinhVien(mssv.trim());
    }

    /** Tạo TaiKhoan (vai trò SINH_VIEN) + SinhVien trong cùng một transaction. */
    @Transactional
    public void registerStudent(RegisterForm f) {
        VaiTro role = vaiTroRepository.findByTenVaiTro(Roles.SINH_VIEN)
                .orElseThrow(() -> new IllegalStateException("Thiếu vai trò SINH_VIEN trong bảng VaiTro"));

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(f.getTenDangNhap().trim());
        tk.setMatKhau(passwordEncoder.encode(f.getMatKhau()));
        tk.setVaiTro(role);
        tk.setTrangThai(TaiKhoan.HOAT_DONG);
        tk = taiKhoanRepository.save(tk);

        SinhVien sv = new SinhVien();
        sv.setTaiKhoan(tk);
        sv.setMaSoSinhVien(f.getMaSoSinhVien().trim());
        sv.setHoTen(f.getHoTen().trim());
        sv.setEmail(f.getEmail().trim());
        sv.setSoDienThoai(blankToNull(f.getSoDienThoai()));
        sv.setLop(blankToNull(f.getLop()));
        sv.setKhoa(blankToNull(f.getKhoa()));
        sinhVienRepository.save(sv);
    }

    /**
     * Quên mật khẩu: xác minh tên đăng nhập + email đã đăng ký của sinh viên.
     * @return mã tài khoản nếu khớp, rỗng nếu không khớp (không tiết lộ là sai tên hay sai email).
     */
    @Transactional(readOnly = true)
    public Optional<Integer> verifyForReset(String username, String email) {
        return taiKhoanRepository.findByTenDangNhap(username.trim())
                .flatMap(tk -> sinhVienRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan()))
                .filter(sv -> sv.getEmail() != null && sv.getEmail().trim().equalsIgnoreCase(email.trim()))
                .map(sv -> sv.getTaiKhoan().getMaTaiKhoan());
    }

    @Transactional
    public void resetPassword(Integer maTaiKhoan, String newPassword) {
        TaiKhoan tk = taiKhoanRepository.findById(maTaiKhoan)
                .orElseThrow(() -> new IllegalStateException("Tài khoản không tồn tại"));
        tk.setMatKhau(passwordEncoder.encode(newPassword));
        taiKhoanRepository.save(tk);
    }

    @Transactional(readOnly = true)
    public Optional<SinhVien> findStudent(Integer maTaiKhoan) {
        return sinhVienRepository.findByTaiKhoan_MaTaiKhoan(maTaiKhoan);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

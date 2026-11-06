package com.example.QuanLyKTX.student;

import com.example.QuanLyKTX.auth.SinhVienRepository;
import com.example.QuanLyKTX.auth.TaiKhoanRepository;
import com.example.QuanLyKTX.entity.SinhVien;
import com.example.QuanLyKTX.entity.TaiKhoan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Tra cứu sinh viên + khóa/mở khóa tài khoản (Ban quản lý, Nhân viên quản lý). */
@Service
public class StudentService {

    public record StudentRow(Integer maSinhVien, String maSoSinhVien, String hoTen, String lop, String khoa,
                             String email, String soDienThoai, boolean biKhoa) {}

    private final SinhVienRepository sinhVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public StudentService(SinhVienRepository sinhVienRepository, TaiKhoanRepository taiKhoanRepository) {
        this.sinhVienRepository = sinhVienRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentRow> search(String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        return sinhVienRepository.search(q).stream()
                .map(s -> new StudentRow(s.getMaSinhVien(), s.getMaSoSinhVien(), s.getHoTen(), s.getLop(),
                        s.getKhoa(), s.getEmail(), s.getSoDienThoai(),
                        TaiKhoan.KHOA.equals(s.getTaiKhoan().getTrangThai())))
                .toList();
    }

    /** Trả về Optional(true) nếu giờ đang khóa, empty nếu không tìm thấy sinh viên. */
    @Transactional
    public Optional<Boolean> toggleLock(Integer maSinhVien) {
        return sinhVienRepository.findById(maSinhVien).map(SinhVien::getTaiKhoan).map(tk -> {
            boolean nowLocked = !TaiKhoan.KHOA.equals(tk.getTrangThai());
            tk.setTrangThai(nowLocked ? TaiKhoan.KHOA : TaiKhoan.HOAT_DONG);
            taiKhoanRepository.save(tk);
            return nowLocked;
        });
    }
}

package com.example.QuanLyKTX.staff;

import com.example.QuanLyKTX.auth.TaiKhoanRepository;
import com.example.QuanLyKTX.auth.VaiTroRepository;
import com.example.QuanLyKTX.common.Roles;
import com.example.QuanLyKTX.entity.TaiKhoan;
import com.example.QuanLyKTX.entity.VaiTro;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Nghiệp vụ quản lý nhân viên (dành cho Ban quản lý).
 * Chỉ thao tác trên 3 vai trò nhân viên; không đụng tới tài khoản Ban quản lý hay sinh viên.
 */
@Service
public class StaffService {

    /** Vai trò nhân viên được phép cấp: mã vai trò -> tên hiển thị. */
    public static final Map<String, String> ROLE_OPTIONS = roleOptions();

    private static Map<String, String> roleOptions() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(Roles.NV_QUAN_LY, Roles.label(Roles.NV_QUAN_LY));
        m.put(Roles.NV_THU_PHI, Roles.label(Roles.NV_THU_PHI));
        m.put(Roles.NV_BAO_TRI, Roles.label(Roles.NV_BAO_TRI));
        return m;
    }

    private static final List<String> STAFF_ROLES = List.copyOf(ROLE_OPTIONS.keySet());

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(TaiKhoanRepository taiKhoanRepository, VaiTroRepository vaiTroRepository,
                        PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static boolean isStaffRole(String role) {
        return role != null && STAFF_ROLES.contains(role);
    }

    public boolean usernameExists(String username) {
        return taiKhoanRepository.existsByTenDangNhap(username.trim());
    }

    @Transactional(readOnly = true)
    public List<TaiKhoan> search(String keyword, String role) {
        List<String> roles = isStaffRole(role) ? List.of(role) : STAFF_ROLES;
        String q = keyword == null ? "" : keyword.trim().toLowerCase();
        return taiKhoanRepository.findByVaiTro_TenVaiTroInOrderByTenDangNhap(roles).stream()
                .filter(t -> q.isEmpty() || t.getTenDangNhap().toLowerCase().contains(q))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TaiKhoan> findStaff(Integer maTaiKhoan) {
        return taiKhoanRepository.findById(maTaiKhoan)
                .filter(t -> isStaffRole(t.getVaiTro().getTenVaiTro()));
    }

    @Transactional
    public void create(StaffForm f) {
        VaiTro role = vaiTroRepository.findByTenVaiTro(f.getVaiTro())
                .orElseThrow(() -> new IllegalStateException("Thiếu vai trò " + f.getVaiTro() + " trong bảng VaiTro"));
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(f.getTenDangNhap().trim());
        tk.setMatKhau(passwordEncoder.encode(f.getMatKhau()));
        tk.setVaiTro(role);
        tk.setTrangThai(TaiKhoan.HOAT_DONG);
        taiKhoanRepository.save(tk);
    }

    /** Đổi vai trò và (nếu newPassword không rỗng) đặt lại mật khẩu. */
    @Transactional
    public boolean update(Integer maTaiKhoan, String newRole, String newPassword) {
        Optional<TaiKhoan> opt = findStaff(maTaiKhoan);
        if (opt.isEmpty() || !isStaffRole(newRole)) return false;
        TaiKhoan tk = opt.get();
        VaiTro role = vaiTroRepository.findByTenVaiTro(newRole)
                .orElseThrow(() -> new IllegalStateException("Thiếu vai trò " + newRole + " trong bảng VaiTro"));
        tk.setVaiTro(role);
        if (newPassword != null && !newPassword.isBlank()) {
            tk.setMatKhau(passwordEncoder.encode(newPassword));
        }
        taiKhoanRepository.save(tk);
        return true;
    }

    /** Khóa <-> mở khóa. Trả về Optional(true) nếu giờ đang khóa, Optional.empty nếu không tìm thấy nhân viên. */
    @Transactional
    public Optional<Boolean> toggleLock(Integer maTaiKhoan) {
        return findStaff(maTaiKhoan).map(tk -> {
            boolean nowLocked = !TaiKhoan.KHOA.equals(tk.getTrangThai());
            tk.setTrangThai(nowLocked ? TaiKhoan.KHOA : TaiKhoan.HOAT_DONG);
            taiKhoanRepository.save(tk);
            return nowLocked;
        });
    }
}

package com.example.QuanLyKTX.maintenance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** SQL bám đúng QuanLyKTX.sql; không tạo bảng hoặc sửa entity dùng chung của nhóm. */
@Repository
public class MaintenanceRepository {
    public record Feedback(Integer id, Integer studentId, String studentName, String studentCode,
                           String room, String equipment, String title, String description,
                           LocalDateTime happenedAt, LocalDateTime submittedAt, String severity,
                           String status, Integer receiverId, String rejectReason,
                           Integer assignmentId, Integer technicianId, String technicianName,
                           LocalDateTime dueAt, String inspection, String repair,
                           BigDecimal cost, String approvalStatus, String approvalNote) {
        public String statusLabel() { return MaintenanceLabels.feedback(status); }
        public String severityLabel() { return MaintenanceLabels.severity(severity); }
        public String approvalLabel() { return MaintenanceLabels.approval(approvalStatus); }
    }
    public record Option(Integer id, String label) {}
    public record RoomOption(String id, String label) {}
    public record Log(Integer id, String previousStatus, String status, String note,
                      String by, LocalDateTime at) {}
    public record Material(Integer id, String name, int quantity, BigDecimal unitPrice, BigDecimal amount) {}
    public record Attachment(Integer id, Integer feedbackId, String path) {}

    private final JdbcTemplate jdbc;
    public MaintenanceRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static LocalDateTime dt(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
    private static Integer optionalId(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int id = rs.getInt(column);
        return rs.wasNull() ? null : id;
    }

    private static final String FEEDBACK_SELECT = """
        SELECT f.MaPhanAnh, f.MaSinhVien, sv.HoTen, sv.MaSoSinhVien, f.MaPhong,
               f.TenThietBi, f.TieuDe, f.MoTa, f.ThoiDiemXayRa, f.NgayGui,
               f.MucDo, f.TrangThai, f.NguoiTiepNhan, f.LyDoTuChoi,
               pc.MaPhanCong, pc.MaNhanVienBaoTri, tech.TenDangNhap AS TenKyThuat,
               pc.HanXuLy, pc.KetQuaKiemTra, pc.NoiDungSuaChua,
               pc.TongChiPhi, pc.TrangThaiDuyet, pc.GhiChuDuyet
          FROM PhanAnhBaoTri f
          JOIN SinhVien sv ON sv.MaSinhVien = f.MaSinhVien
          LEFT JOIN PhanCongBaoTri pc ON pc.MaPhanCong = (
              SELECT MAX(x.MaPhanCong) FROM PhanCongBaoTri x WHERE x.MaPhanAnh = f.MaPhanAnh)
          LEFT JOIN TaiKhoan tech ON tech.MaTaiKhoan = pc.MaNhanVienBaoTri
        """;
    private static final RowMapper<Feedback> FEEDBACK_MAPPER = (rs, n) -> new Feedback(
        rs.getInt("MaPhanAnh"), rs.getInt("MaSinhVien"), rs.getString("HoTen"),
        rs.getString("MaSoSinhVien"), rs.getString("MaPhong"), rs.getString("TenThietBi"),
        rs.getString("TieuDe"), rs.getString("MoTa"), dt(rs,"ThoiDiemXayRa"),
        dt(rs,"NgayGui"), rs.getString("MucDo"), rs.getString("TrangThai"),
        optionalId(rs,"NguoiTiepNhan"), rs.getString("LyDoTuChoi"),
        optionalId(rs,"MaPhanCong"), optionalId(rs,"MaNhanVienBaoTri"),
        rs.getString("TenKyThuat"), dt(rs,"HanXuLy"), rs.getString("KetQuaKiemTra"),
        rs.getString("NoiDungSuaChua"), rs.getBigDecimal("TongChiPhi"),
        rs.getString("TrangThaiDuyet"), rs.getString("GhiChuDuyet"));

    public List<Feedback> search(String q, String status, Integer studentId, Integer technicianId) {
        StringBuilder sql = new StringBuilder(FEEDBACK_SELECT).append(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        if (studentId != null) { sql.append(" AND f.MaSinhVien=? "); args.add(studentId); }
        if (technicianId != null) { sql.append(" AND pc.MaNhanVienBaoTri=? "); args.add(technicianId); }
        if (status != null && !status.isBlank()) { sql.append(" AND f.TrangThai=? "); args.add(status); }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (LOWER(f.TieuDe) LIKE ? OR LOWER(sv.HoTen) LIKE ? OR LOWER(f.MaPhong) LIKE ? OR CAST(f.MaPhanAnh AS CHAR) LIKE ?) ");
            String pattern = "%" + q.strip().toLowerCase() + "%";
            for (int i = 0; i < 4; i++) args.add(pattern);
        }
        sql.append(" ORDER BY f.NgayGui DESC, f.MaPhanAnh DESC ");
        return jdbc.query(sql.toString(), FEEDBACK_MAPPER, args.toArray());
    }
    public Optional<Feedback> find(int id) {
        List<Feedback> found = jdbc.query(FEEDBACK_SELECT + " WHERE f.MaPhanAnh=?", FEEDBACK_MAPPER, id);
        return found.stream().findFirst();
    }
    public Optional<Integer> studentIdForAccount(int accountId) {
        List<Integer> ids = jdbc.query("SELECT MaSinhVien FROM SinhVien WHERE MaTaiKhoan=?",
            (rs, i) -> rs.getInt(1), accountId);
        return ids.stream().findFirst();
    }
    public List<RoomOption> studentRooms(int studentId) {
        return jdbc.query("""
            SELECT DISTINCT p.MaPhong, p.TenPhong
              FROM PhanBoPhong pb JOIN Phong p ON p.MaPhong=pb.MaPhong
             WHERE pb.MaSinhVien=? AND pb.NgayRa IS NULL AND pb.TrangThai='Đang lưu trú'
             ORDER BY p.MaPhong
            """, (rs, i) -> new RoomOption(rs.getString(1), rs.getString(2)), studentId);
    }
    public boolean roomBelongsToStudent(int studentId, String room) {
        return !jdbc.query("""
            SELECT pb.MaPhanBo FROM PhanBoPhong pb
             WHERE pb.MaSinhVien=? AND pb.MaPhong=? AND pb.NgayRa IS NULL
               AND pb.TrangThai='Đang lưu trú' LIMIT 1
            """, (rs, i) -> rs.getInt(1), studentId, room).isEmpty();
    }
    public List<Option> technicians() {
        return jdbc.query("""
            SELECT t.MaTaiKhoan, t.TenDangNhap FROM TaiKhoan t
              JOIN VaiTro v ON v.MaVaiTro=t.MaVaiTro
             WHERE v.TenVaiTro='NHAN_VIEN_BAO_TRI' AND t.TrangThai='HOAT_DONG'
             ORDER BY t.TenDangNhap
            """, (rs, i) -> new Option(rs.getInt(1),rs.getString(2)));
    }
    public boolean isTechnician(int accountId) {
        return !jdbc.query("""
            SELECT t.MaTaiKhoan FROM TaiKhoan t JOIN VaiTro v ON v.MaVaiTro=t.MaVaiTro
             WHERE t.MaTaiKhoan=? AND t.TrangThai='HOAT_DONG'
               AND v.TenVaiTro='NHAN_VIEN_BAO_TRI'
            """, (rs, i) -> rs.getInt(1), accountId).isEmpty();
    }
    public List<Option> students() {
        return jdbc.query("SELECT MaSinhVien, CONCAT(MaSoSinhVien, ' - ', HoTen) FROM SinhVien ORDER BY HoTen",
            (rs, i) -> new Option(rs.getInt(1), rs.getString(2)));
    }
    public boolean studentExists(int studentId) {
        return !jdbc.query("SELECT MaSinhVien FROM SinhVien WHERE MaSinhVien=?",
            (rs, i) -> rs.getInt(1), studentId).isEmpty();
    }
    public int create(int studentId, String room, String equipment, String title, String description,
                      LocalDateTime happened, String severity) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                INSERT INTO PhanAnhBaoTri(MaSinhVien,MaPhong,TenThietBi,TieuDe,MoTa,ThoiDiemXayRa,MucDo,TrangThai)
                VALUES(?,?,?,?,?,?,?,'MOI_GUI')
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1,studentId); ps.setString(2,room); ps.setString(3,equipment);
            ps.setString(4,title); ps.setString(5,description);
            ps.setTimestamp(6,happened == null ? null : Timestamp.valueOf(happened));
            ps.setString(7,severity);
            return ps;
        }, key);
        return key.getKey().intValue();
    }
    public void saveAttachment(int feedbackId, String path) {
        jdbc.update("INSERT INTO AnhPhanAnh(MaPhanAnh,DuongDanAnh) VALUES(?,?)",feedbackId,path);
    }
    public Optional<Attachment> attachment(int id) {
        List<Attachment> rows = jdbc.query(
            "SELECT MaAnh,MaPhanAnh,DuongDanAnh FROM AnhPhanAnh WHERE MaAnh=?",
            (rs,i) -> new Attachment(rs.getInt(1),rs.getInt(2),rs.getString(3)),id);
        return rows.stream().findFirst();
    }
    public List<Attachment> attachments(int feedbackId) {
        return jdbc.query("SELECT MaAnh,MaPhanAnh,DuongDanAnh FROM AnhPhanAnh WHERE MaPhanAnh=? ORDER BY MaAnh",
            (rs,i) -> new Attachment(rs.getInt(1),rs.getInt(2),rs.getString(3)), feedbackId);
    }
    public void receive(int id, int managerId) {
        jdbc.update("UPDATE PhanAnhBaoTri SET TrangThai='DANG_TIEP_NHAN',NguoiTiepNhan=? WHERE MaPhanAnh=?",managerId,id);
    }
    public void reject(int id, int managerId, String reason) {
        jdbc.update("UPDATE PhanAnhBaoTri SET TrangThai='TU_CHOI',NguoiTiepNhan=?,LyDoTuChoi=? WHERE MaPhanAnh=?",
            managerId,reason,id);
    }
    public int assign(int id, int technicianId, int managerId, LocalDateTime due) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                INSERT INTO PhanCongBaoTri(MaPhanAnh,MaNhanVienBaoTri,NguoiPhanCong,HanXuLy)
                VALUES(?,?,?,?)
                """,Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1,id); ps.setInt(2,technicianId); ps.setInt(3,managerId);
            ps.setTimestamp(4,due == null ? null : Timestamp.valueOf(due));
            return ps;
        },key);
        jdbc.update("UPDATE PhanAnhBaoTri SET TrangThai='DA_PHAN_CONG',NguoiTiepNhan=COALESCE(NguoiTiepNhan,?) WHERE MaPhanAnh=?",managerId,id);
        return key.getKey().intValue();
    }
    public void setFeedbackStatus(int id, String status) {
        jdbc.update("UPDATE PhanAnhBaoTri SET TrangThai=? WHERE MaPhanAnh=?",status,id);
    }
    public void addMaterial(int assignmentId,String name,int qty,BigDecimal price) {
        jdbc.update("""
            INSERT INTO VatTuBaoTri(MaPhanCong,TenVatTu,SoLuong,DonGia,ThanhTien)
            VALUES(?,?,?,?,?)
            """,assignmentId,name,qty,price,price.multiply(BigDecimal.valueOf(qty)));
        jdbc.update("""
    UPDATE PhanCongBaoTri SET TongChiPhi=(
      SELECT COALESCE(SUM(v.ThanhTien),0)
      FROM VatTuBaoTri v
      WHERE v.MaPhanCong=?),
      TrangThaiDuyet='CHO_DUYET',
      NguoiDuyet=NULL,
      NgayDuyet=NULL,
      GhiChuDuyet=NULL
    WHERE MaPhanCong=?
    """,assignmentId,assignmentId);
    }
    public boolean removeMaterial(int assignmentId,int materialId) {
        int rows=jdbc.update("DELETE FROM VatTuBaoTri WHERE MaVatTu=? AND MaPhanCong=?",materialId,assignmentId);
        if(rows==0)return false;
        jdbc.update("""
            UPDATE PhanCongBaoTri SET TongChiPhi=(
              SELECT COALESCE(SUM(v.ThanhTien),0) FROM VatTuBaoTri v WHERE v.MaPhanCong=?),
              TrangThaiDuyet=CASE WHEN (
                SELECT COALESCE(SUM(v.ThanhTien),0) FROM VatTuBaoTri v WHERE v.MaPhanCong=?) > 0
                THEN 'CHO_DUYET' ELSE 'KHONG_CAN' END,
              NguoiDuyet=NULL, NgayDuyet=NULL, GhiChuDuyet=NULL
            WHERE MaPhanCong=?
            """,assignmentId,assignmentId,assignmentId);
        return true;
    }
    public List<Material> materials(int assignmentId) {
        return jdbc.query("SELECT MaVatTu,TenVatTu,SoLuong,DonGia,ThanhTien FROM VatTuBaoTri WHERE MaPhanCong=? ORDER BY MaVatTu",
            (rs,i) -> new Material(rs.getInt(1),rs.getString(2),rs.getInt(3),rs.getBigDecimal(4),rs.getBigDecimal(5)),assignmentId);
    }
    public void updateRepair(int assignmentId, String inspection, String repair) {
        jdbc.update("UPDATE PhanCongBaoTri SET KetQuaKiemTra=?,NoiDungSuaChua=? WHERE MaPhanCong=?",
            inspection, repair, assignmentId);
    }
    public void decision(int assignmentId,int managerId, boolean approve,String note) {
        jdbc.update("""
            UPDATE PhanCongBaoTri SET TrangThaiDuyet=?,NguoiDuyet=?,NgayDuyet=NOW(),GhiChuDuyet=?
            WHERE MaPhanCong=?
            """,approve ? "DA_DUYET" : "TU_CHOI",managerId,note,assignmentId);
    }
    public void complete(int assignmentId) {
        jdbc.update("UPDATE PhanCongBaoTri SET NgayHoanThanh=NOW() WHERE MaPhanCong=?",assignmentId);
    }
    public void log(int feedbackId,Integer assignmentId,String previous,String next,String note,int userId) {
        jdbc.update("""
            INSERT INTO LichSuBaoTri(MaPhanAnh,MaPhanCong,TrangThaiCu,TrangThaiMoi,NoiDung,NguoiThucHien)
            VALUES(?,?,?,?,?,?)
            """,feedbackId,assignmentId,previous,next,note,userId);
    }
    public List<Log> history(int feedbackId) {
        return jdbc.query("""
            SELECT l.MaLichSu,l.TrangThaiCu,l.TrangThaiMoi,l.NoiDung,t.TenDangNhap,l.ThoiGian
              FROM LichSuBaoTri l LEFT JOIN TaiKhoan t ON t.MaTaiKhoan=l.NguoiThucHien
             WHERE l.MaPhanAnh=? ORDER BY l.ThoiGian DESC,l.MaLichSu DESC
            """,(rs,i) -> new Log(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
                rs.getString(5),dt(rs,"ThoiGian")),feedbackId);
    }
}

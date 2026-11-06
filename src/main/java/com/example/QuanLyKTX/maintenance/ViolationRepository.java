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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ViolationRepository {
    public record Violation(Integer id,Integer studentId,String studentName,String studentCode,
                            String room,LocalDateTime happenedAt,String place,String description,
                            String severity,String status,LocalDateTime createdAt,
                            String recordedBy,String resolution,BigDecimal fine,String resolutionNote,
                            LocalDateTime resolvedAt) {
        public String statusLabel() { return resolution != null ? "Đã xử lý" : MaintenanceLabels.violation(status); }
        public String severityLabel() { return MaintenanceLabels.violationSeverity(severity); }
    }
    private final JdbcTemplate jdbc;
    public ViolationRepository(JdbcTemplate jdbc) {this.jdbc=jdbc;}
    private static LocalDateTime date(java.sql.ResultSet rs,String key) throws java.sql.SQLException {
        Timestamp t=rs.getTimestamp(key);
        return t==null ? null:t.toLocalDateTime();
    }
    private static final String SELECT = """
       SELECT b.MaBienBan,b.MaSinhVien,s.HoTen,s.MaSoSinhVien,b.MaPhong,b.ThoiGianViPham,
              b.DiaDiem,b.NoiDung,b.MucDo,b.TrangThai,b.NgayLap,t.TenDangNhap,
              x.HinhThucXuLy,x.SoTienPhat,x.GhiChu,x.NgayQuyetDinh
         FROM BienBanViPham b JOIN SinhVien s ON s.MaSinhVien=b.MaSinhVien
         JOIN TaiKhoan t ON t.MaTaiKhoan=b.NguoiLap
         LEFT JOIN XuLyViPham x ON x.MaBienBan=b.MaBienBan
       """;
    private static final RowMapper<Violation> MAPPER=(rs,i)->new Violation(
        rs.getInt("MaBienBan"),rs.getInt("MaSinhVien"),rs.getString("HoTen"),
        rs.getString("MaSoSinhVien"),rs.getString("MaPhong"),date(rs,"ThoiGianViPham"),
        rs.getString("DiaDiem"),rs.getString("NoiDung"),rs.getString("MucDo"),
        rs.getString("TrangThai"),date(rs,"NgayLap"),rs.getString("TenDangNhap"),
        rs.getString("HinhThucXuLy"),rs.getBigDecimal("SoTienPhat"),
        rs.getString("GhiChu"),date(rs,"NgayQuyetDinh"));
    public List<Violation> search(String q,String status,Integer studentId){
        return search(q,status,studentId,null,null,null);
    }
    public List<Violation> search(String q,String status,Integer studentId,
                                  LocalDate from,LocalDate to,String severity){
        StringBuilder sql=new StringBuilder(SELECT).append(" WHERE 1=1 ");
        List<Object> args=new ArrayList<>();
        if(studentId!=null){sql.append(" AND b.MaSinhVien=? ");args.add(studentId);}
        if(from!=null){sql.append(" AND b.ThoiGianViPham>=? ");args.add(from.atStartOfDay());}
        if(to!=null){sql.append(" AND b.ThoiGianViPham<? ");args.add(to.plusDays(1).atStartOfDay());}
        if(severity!=null&&!severity.isBlank()){
            sql.append(" AND b.MucDo=? ");args.add(severity);
        }
        if(status!=null&&!status.isBlank()){
            if("DA_XU_LY".equals(status)) sql.append(" AND x.MaXuLy IS NOT NULL ");
            else {sql.append(" AND b.TrangThai=? AND x.MaXuLy IS NULL ");args.add(status);}
        }
        if(q!=null&&!q.isBlank()){
            sql.append(" AND (LOWER(s.HoTen) LIKE ? OR LOWER(s.MaSoSinhVien) LIKE ? OR LOWER(b.NoiDung) LIKE ?) ");
            String pattern="%"+q.strip().toLowerCase()+"%";
            args.add(pattern);args.add(pattern);args.add(pattern);
        }
        sql.append(" ORDER BY b.NgayLap DESC,b.MaBienBan DESC ");
        return jdbc.query(sql.toString(),MAPPER,args.toArray());
    }
    public Optional<Violation> find(int id){
        return jdbc.query(SELECT+" WHERE b.MaBienBan=?",MAPPER,id).stream().findFirst();
    }
    public int create(int studentId,String room,int authorId,LocalDateTime at,String place,String text,String level){
        KeyHolder key=new GeneratedKeyHolder();
        jdbc.update(con->{
            PreparedStatement ps=con.prepareStatement("""
                INSERT INTO BienBanViPham(MaSinhVien,MaPhong,NguoiLap,ThoiGianViPham,DiaDiem,NoiDung,MucDo,TrangThai)
                VALUES(?,?,?,?,?,?,?,'MOI_LAP')
                """,Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1,studentId); ps.setString(2,room);ps.setInt(3,authorId);
            ps.setTimestamp(4,Timestamp.valueOf(at));ps.setString(5,place);
            ps.setString(6,text);ps.setString(7,level);return ps;
        },key);
        return key.getKey().intValue();
    }
    public void requestReview(int id){
        jdbc.update("UPDATE BienBanViPham SET TrangThai='CHO_XEM_XET' WHERE MaBienBan=?",id);
    }
    public void decide(int id,int managerId,String method,BigDecimal fine,String note){
        jdbc.update("""
            INSERT INTO XuLyViPham(MaBienBan,NguoiQuyetDinh,HinhThucXuLy,SoTienPhat,GhiChu)
            VALUES(?,?,?,?,?)
            """,id,managerId,method,fine,note);
        jdbc.update("UPDATE BienBanViPham SET TrangThai='DA_XU_LY' WHERE MaBienBan=?",id);
    }
}

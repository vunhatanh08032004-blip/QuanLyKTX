package com.example.QuanLyKTX.auth;

import com.example.QuanLyKTX.entity.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, Integer> {
    Optional<SinhVien> findByTaiKhoan_MaTaiKhoan(Integer maTaiKhoan);
    boolean existsByMaSoSinhVien(String maSoSinhVien);

    /** Tìm theo họ tên / MSSV / lớp / khoa. Truyền q = "" để lấy tất cả. */
    @Query("select s from SinhVien s join fetch s.taiKhoan t "
         + "where lower(s.hoTen) like lower(concat('%', :q, '%')) "
         + "or lower(s.maSoSinhVien) like lower(concat('%', :q, '%')) "
         + "or lower(coalesce(s.lop, '')) like lower(concat('%', :q, '%')) "
         + "or lower(coalesce(s.khoa, '')) like lower(concat('%', :q, '%')) "
         + "order by s.maSoSinhVien")
    List<SinhVien> search(@Param("q") String q);
}

package com.example.QuanLyKTX.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "SinhVien")
public class SinhVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSinhVien")
    private Integer maSinhVien;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaTaiKhoan", nullable = false, unique = true)
    private TaiKhoan taiKhoan;

    @Column(name = "MaSoSinhVien", nullable = false, unique = true, length = 20)
    private String maSoSinhVien;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    /** NAM / NU / KHAC */
    @Column(name = "GioiTinh")
    private String gioiTinh;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "Lop", length = 50)
    private String lop;

    @Column(name = "Khoa", length = 100)
    private String khoa;

    public Integer getMaSinhVien() { return maSinhVien; }
    public void setMaSinhVien(Integer maSinhVien) { this.maSinhVien = maSinhVien; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public String getMaSoSinhVien() { return maSoSinhVien; }
    public void setMaSoSinhVien(String maSoSinhVien) { this.maSoSinhVien = maSoSinhVien; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }
    public String getKhoa() { return khoa; }
    public void setKhoa(String khoa) { this.khoa = khoa; }
}

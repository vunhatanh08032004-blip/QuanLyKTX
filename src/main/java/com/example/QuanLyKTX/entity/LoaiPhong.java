package com.example.QuanLyKTX.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "LoaiPhong")
public class LoaiPhong {

    @Id
    @Column(name = "MaLoaiPhong", length = 20)
    private String maLoaiPhong;

    @Column(name = "TenLoai", nullable = false, length = 100)
    private String tenLoai;

    @Column(name = "SucChua", nullable = false)
    private int sucChua;

    @Column(name = "GiaCoBan", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaCoBan;

    // Quan hệ 1 Loại phòng áp dụng cho nhiều Phòng
    @OneToMany(mappedBy = "loaiPhong", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Phong> danhSachPhong;

    public String getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(String maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public int getSucChua() { return sucChua; }
    public void setSucChua(int sucChua) { this.sucChua = sucChua; }
    public BigDecimal getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(BigDecimal giaCoBan) { this.giaCoBan = giaCoBan; }
    public List<Phong> getDanhSachPhong() { return danhSachPhong; }
    public void setDanhSachPhong(List<Phong> danhSachPhong) { this.danhSachPhong = danhSachPhong; }
}
package com.example.QuanLyKTX.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ToaNha")
public class ToaNha {

    @Id
    @Column(name = "MaToaNha", length = 20)
    private String maToaNha;

    @Column(name = "TenToaNha", nullable = false, length = 100)
    private String tenToaNha;

    @Column(name = "SoTang", nullable = false)
    private int soTang;

    @Column(name = "LoaiToa", length = 50)
    private String loaiToa;

    // Quan hệ 1 Tòa nhà có nhiều Phòng
    @OneToMany(mappedBy = "toaNha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Phong> danhSachPhong;

    public String getMaToaNha() { return maToaNha; }
    public void setMaToaNha(String maToaNha) { this.maToaNha = maToaNha; }
    public String getTenToaNha() { return tenToaNha; }
    public void setTenToaNha(String tenToaNha) { this.tenToaNha = tenToaNha; }
    public int getSoTang() { return soTang; }
    public void setSoTang(int soTang) { this.soTang = soTang; }
    public String getLoaiToa() { return loaiToa; }
    public void setLoaiToa(String loaiToa) { this.loaiToa = loaiToa; }
    public List<Phong> getDanhSachPhong() { return danhSachPhong; }
    public void setDanhSachPhong(List<Phong> danhSachPhong) { this.danhSachPhong = danhSachPhong; }
}
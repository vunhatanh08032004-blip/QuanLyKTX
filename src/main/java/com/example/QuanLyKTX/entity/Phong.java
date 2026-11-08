package com.example.QuanLyKTX.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Phong")
public class Phong {

    @Id
    @Column(name = "MaPhong", length = 20)
    private String maPhong;

    @Column(name = "TenPhong", nullable = false, length = 50)
    private String tenPhong;

    // Khóa ngoại trỏ đến bảng ToaNha
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaToaNha", nullable = false)
    private ToaNha toaNha;

    // Khóa ngoại trỏ đến bảng LoaiPhong
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaLoaiPhong", nullable = false)
    private LoaiPhong loaiPhong;

    @Column(name = "SucChua", nullable = false)
    private int sucChua;

    @Column(name = "SoNguoiHienTai")
    private int soNguoiHienTai = 0;

    @Column(name = "TrangThai", length = 50)
    private String trangThai = "Trống";

    // Quan hệ 1 Phòng có nhiều Lịch sử phân bổ
    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhanBoPhong> danhSachPhanBo;

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public ToaNha getToaNha() { return toaNha; }
    public void setToaNha(ToaNha toaNha) { this.toaNha = toaNha; }
    public LoaiPhong getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(LoaiPhong loaiPhong) { this.loaiPhong = loaiPhong; }
    public int getSucChua() { return sucChua; }
    public void setSucChua(int sucChua) { this.sucChua = sucChua; }
    public int getSoNguoiHienTai() { return soNguoiHienTai; }
    public void setSoNguoiHienTai(int soNguoiHienTai) { this.soNguoiHienTai = soNguoiHienTai; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public List<PhanBoPhong> getDanhSachPhanBo() { return danhSachPhanBo; }
    public void setDanhSachPhanBo(List<PhanBoPhong> danhSachPhanBo) { this.danhSachPhanBo = danhSachPhanBo; }
}
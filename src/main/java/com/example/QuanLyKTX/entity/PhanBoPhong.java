package com.example.QuanLyKTX.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "PhanBoPhong")
public class PhanBoPhong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhanBo")
    private Integer maPhanBo;

    // Khóa ngoại trỏ đến bảng Phong
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaPhong", nullable = false)
    private Phong phong;

    // Khóa ngoại trỏ đến bảng SinhVien (Phần của Bình)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaSinhVien", nullable = false)
    private SinhVien sinhVien;

    @Column(name = "NgayVao", nullable = false)
    private LocalDate ngayVao;

    @Column(name = "NgayRa")
    private LocalDate ngayRa;

    @Column(name = "TrangThai", length = 50)
    private String trangThai = "Đang lưu trú";

    public Integer getMaPhanBo() { return maPhanBo; }
    public void setMaPhanBo(Integer maPhanBo) { this.maPhanBo = maPhanBo; }
    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }
    public SinhVien getSinhVien() { return sinhVien; }
    public void setSinhVien(SinhVien sinhVien) { this.sinhVien = sinhVien; }
    public LocalDate getNgayVao() { return ngayVao; }
    public void setNgayVao(LocalDate ngayVao) { this.ngayVao = ngayVao; }
    public LocalDate getNgayRa() { return ngayRa; }
    public void setNgayRa(LocalDate ngayRa) { this.ngayRa = ngayRa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
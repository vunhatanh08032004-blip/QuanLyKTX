package com.example.QuanLyKTX.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TaiKhoan")
public class TaiKhoan {

    public static final String HOAT_DONG = "HOAT_DONG";
    public static final String KHOA = "KHOA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTaiKhoan")
    private Integer maTaiKhoan;

    @Column(name = "TenDangNhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "MatKhau", nullable = false)
    private String matKhau;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "MaVaiTro", nullable = false)
    private VaiTro vaiTro;

    /** HOAT_DONG hoặc KHOA (cột ENUM trong MySQL, ánh xạ dạng chuỗi). */
    @Column(name = "TrangThai")
    private String trangThai = HOAT_DONG;

    public Integer getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(Integer maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

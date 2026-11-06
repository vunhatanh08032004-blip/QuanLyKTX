package com.example.QuanLyKTX.common;

/** Dữ liệu người dùng hiện tại để layout hiển thị (header + sidebar). */
public class CurrentUserView {

    private final String hoTen;
    private final String tenDangNhap;
    private final String vaiTro;
    private final String vaiTroLabel;

    public CurrentUserView(String hoTen, String tenDangNhap, String vaiTro) {
        this.hoTen = hoTen;
        this.tenDangNhap = tenDangNhap;
        this.vaiTro = vaiTro;
        this.vaiTroLabel = Roles.label(vaiTro);
    }

    public String getHoTen() { return hoTen; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getVaiTro() { return vaiTro; }
    public String getVaiTroLabel() { return vaiTroLabel; }

    /** Chữ cái đầu của tên, dùng cho avatar. */
    public String getInitial() {
        String name = (hoTen == null || hoTen.isBlank()) ? tenDangNhap : hoTen.trim();
        String[] parts = name.split("\\s+");
        String last = parts[parts.length - 1];
        return last.substring(0, 1).toUpperCase();
    }
}

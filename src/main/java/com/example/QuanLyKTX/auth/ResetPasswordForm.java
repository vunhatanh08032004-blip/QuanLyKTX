package com.example.QuanLyKTX.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordForm {

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, max = 50, message = "Mật khẩu từ 6 đến 50 ký tự")
    private String matKhau;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới")
    private String xacNhanMatKhau;

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getXacNhanMatKhau() { return xacNhanMatKhau; }
    public void setXacNhanMatKhau(String xacNhanMatKhau) { this.xacNhanMatKhau = xacNhanMatKhau; }
}

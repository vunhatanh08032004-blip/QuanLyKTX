package com.example.QuanLyKTX.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Mật khẩu mới luôn được mã hóa BCrypt.
 * Riêng dữ liệu mẫu trong QuanLyKTX.sql đang lưu mật khẩu thô ("123") nên khi hash không bắt đầu
 * bằng "$2" ta vẫn cho đăng nhập bằng so sánh thường, để cả nhóm test được ngay.
 * Khi nộp bài nên đổi sang BCrypt hết (đăng ký mới / quên mật khẩu sẽ tự lưu BCrypt).
 */
public class AppPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;
        if (encodedPassword.startsWith("$2")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        return MessageDigest.isEqual(
                rawPassword.toString().getBytes(StandardCharsets.UTF_8),
                encodedPassword.getBytes(StandardCharsets.UTF_8));
    }
}

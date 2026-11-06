package com.example.QuanLyKTX.registration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "ảo" do Bình tạo để menu không bị 404.
 * Bình: mở file templates/registration/register-form.html, viết giao diện của mình đè lên,
 * rồi thêm Service/Repository và xử lý nghiệp vụ ngay trong class này.
 */
@Controller
public class RegistrationController {

    @GetMapping("/registration")
    public String index() {
        return "registration/register-form";
    }
}

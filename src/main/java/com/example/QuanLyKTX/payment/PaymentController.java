package com.example.QuanLyKTX.payment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "ảo" do Bình tạo để menu không bị 404.
 * Thúy: mở file templates/payment/payment-list.html, viết giao diện của mình đè lên,
 * rồi thêm Service/Repository và xử lý nghiệp vụ ngay trong class này.
 */
@Controller
public class PaymentController {

    @GetMapping("/payment")
    public String index() {
        return "payment/payment-list";
    }
}

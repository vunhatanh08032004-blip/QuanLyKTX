package com.example.QuanLyKTX.report;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "ảo" do Bình tạo để menu không bị 404.
 * Thúy/Bình: mở file templates/report/report-home.html, viết giao diện của mình đè lên,
 * rồi thêm Service/Repository và xử lý nghiệp vụ ngay trong class này.
 */
@Controller
public class ReportController {

    @GetMapping("/report")
    public String index() {
        return "report/report-home";
    }
}

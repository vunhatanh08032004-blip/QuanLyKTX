package com.example.QuanLyKTX.maintenance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "ảo" do Bình tạo để menu không bị 404.
 * Thơ: mở templates/maintenance/feedback-list.html và violation-list.html, viết giao diện đè lên,
 * rồi thêm Service/Repository và xử lý nghiệp vụ ngay trong class này.
 */
@Controller
public class MaintenanceController {

    @GetMapping("/maintenance")
    public String feedbacks() {
        return "maintenance/feedback-list";
    }

    @GetMapping("/maintenance/violation")
    public String violations() {
        return "maintenance/violation-list";
    }
}

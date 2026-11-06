package com.example.QuanLyKTX.contract;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "ảo" do Bình tạo để menu không bị 404.
 * Nhi: mở file templates/contract/contract-list.html, viết giao diện của mình đè lên,
 * rồi thêm Service/Repository và xử lý nghiệp vụ ngay trong class này.
 */
@Controller
public class ContractController {

    @GetMapping("/contract")
    public String index() {
        return "contract/contract-list";
    }
}

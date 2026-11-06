package com.example.QuanLyKTX.contract;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Trang để SINH VIÊN chọn: gia hạn hợp đồng / hủy hợp đồng (không thuê nữa) / chuyển phòng.
 * Chỉ sinh viên vào được (xem SecurityConfig: /contract/request/**).
 * Controller tách riêng để không đụng ContractController của Nhi. Nhi xử lý lưu yêu cầu
 * và Nhân viên quản lý / Ban quản lý duyệt yêu cầu ở trang Quản lý hợp đồng.
 */
@Controller
public class ContractRequestController {

    @GetMapping("/contract/request")
    public String index() {
        return "contract/contract-request";
    }
}

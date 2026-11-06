package com.example.QuanLyKTX.room;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller "ảo" do Bình tạo để menu không bị 404.
 * Bảo: mở file templates/room/room-list.html, viết giao diện của mình đè lên,
 * rồi thêm Service/Repository và xử lý nghiệp vụ ngay trong class này.
 */
@Controller
public class RoomController {

    @GetMapping("/room")
    public String index() {
        return "room/room-list";
    }
}

package com.example.QuanLyKTX.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // Hiển thị giao diện Quản lý Tòa nhà/Phòng ở
    @GetMapping("/list")
    public String showRoomList(Model model) {
        // 1. Gửi danh sách phòng để hiển thị Table
        model.addAttribute("danhSachPhong", roomService.layDanhSachPhong());

        // 2. Gửi danh sách sinh viên đang lưu trú để làm Dropdown cho form Chuyển/Trả phòng
        model.addAttribute("danhSachDangLuuTru", roomService.layDanhSachDangLuuTru());

        //3. Hàm sau này khi ráp code
        // model.addAttribute("danhSachSinhVien", sinhVienService.laySinhVienChuaCoPhong());

        return "room/room-list";
    }

    // Nối đường dẫn xử lý Bố trí phòng
    @PostMapping("/assign")
    public String assignRoom(@RequestParam("maPhong") String maPhong,
                             @RequestParam("maSinhVien") Integer maSinhVien,
                             RedirectAttributes redirectAttributes) {
        boolean success = roomService.boTriPhong(maPhong, maSinhVien);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Bố trí phòng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Phòng đã đầy hoặc không tồn tại!");
        }
        return "redirect:/room/list";
    }

    // Nối đường dẫn xử lý Chuyển phòng
    @PostMapping("/transfer")
    public String transferRoom(@RequestParam("maPhanBo") Integer maPhanBo,
                               @RequestParam("maPhongMoi") String maPhongMoi,
                               RedirectAttributes redirectAttributes) {
        boolean success = roomService.chuyenPhong(maPhanBo, maPhongMoi);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Chuyển phòng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi chuyển phòng. Vui lòng kiểm tra lại phòng đích!");
        }
        return "redirect:/room/list";
    }

    // Nối đường dẫn xử lý Trả phòng
    @PostMapping("/checkout")
    public String checkoutRoom(@RequestParam("maPhanBo") Integer maPhanBo,
                               RedirectAttributes redirectAttributes) {
        boolean success = roomService.traPhong(maPhanBo);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Trả phòng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi trả phòng!");
        }
        return "redirect:/room/list";
    }
}
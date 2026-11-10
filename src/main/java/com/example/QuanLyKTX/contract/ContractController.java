package com.example.QuanLyKTX.contract;

import com.example.QuanLyKTX.entity.HopDong;
import com.example.QuanLyKTX.repository.HopDongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;


import java.util.List;

@Controller
public class ContractController {

    @Autowired
    private HopDongRepository hopDongRepository;

    // 1. Trang danh sách hợp đồng (Tích hợp cả chức năng tìm kiếm)
    @GetMapping("/contract")
    public String index(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<HopDong> listHopDong;
        if (keyword != null && !keyword.trim().isEmpty()) {
            listHopDong = hopDongRepository.searchContracts(keyword);
        } else {
            listHopDong = hopDongRepository.findAll();
        }
        model.addAttribute("listHopDong", listHopDong);
        model.addAttribute("keyword", keyword); // Giữ lại từ khóa trên ô input sau khi tìm
        return "contract/contract-list";
    }

    // 2. Trang form Lập hợp đồng mới
    @GetMapping("/contract/new")
    public String showCreateForm(Model model) {

        // Tạo một đối tượng hợp đồng mới
        model.addAttribute("hopDong", new HopDong());

        return "contract/contract-form";
    }

    // 3. Trang xem Chi tiết hợp đồng theo ID
    @GetMapping("/contract/detail/{id}")
    public String viewDetail(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("hopDong", hopDongRepository.findById(id).orElse(null));
        return "contract/contract-detail";
    }

    // 4. Trang form Sửa hợp đồng theo ID
    @GetMapping("/contract/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {

        // Lấy hợp đồng từ database theo mã hợp đồng
        HopDong hopDong = hopDongRepository.findById(id).orElse(null);

        // Đưa thông tin hợp đồng sang giao diện
        model.addAttribute("hopDong", hopDong);

        return "contract/contract-form";
    }
    // 5. Xóa hợp đồng theo ID
    @GetMapping("/contract/delete/{id}")
    public String deleteContract(@PathVariable("id") Integer id) {
        // Xóa hợp đồng trong database dựa vào JpaRepository có sẵn
        hopDongRepository.deleteById(id);
        // Sau khi xóa xong, chuyển hướng về lại trang danh sách hợp đồng
        return "redirect:/contract";
    }
    // 6. Lưu hợp đồng mới hoặc cập nhật hợp đồng đã sửa
    @PostMapping("/contract/save")
    public String saveContract(@ModelAttribute("hopDong") HopDong hopDong) {


        // Lưu hợp đồng vào database
        hopDongRepository.save(hopDong);

        // Sau khi lưu xong, quay về trang danh sách
        return "redirect:/contract";
    }
}
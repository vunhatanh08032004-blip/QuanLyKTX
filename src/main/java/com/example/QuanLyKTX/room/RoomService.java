package com.example.QuanLyKTX.room;

import com.example.QuanLyKTX.auth.SinhVienRepository;
import com.example.QuanLyKTX.entity.Phong;
import com.example.QuanLyKTX.entity.PhanBoPhong;
import com.example.QuanLyKTX.entity.SinhVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private PhongRepository phongRepository;

    @Autowired
    private PhanBoPhongRepository phanBoPhongRepository;

    @Autowired
    private SinhVienRepository sinhVienRepository;

    public List<Phong> layDanhSachPhong() {
        return phongRepository.findAll();
    }

    public List<PhanBoPhong> layDanhSachDangLuuTru() {
        return phanBoPhongRepository.findAll().stream()
                .filter(pb -> pb.getTrangThai().equals("Đang lưu trú"))
                .toList();
    }

    // 1. Nghiệp vụ Bố trí phòng
    @Transactional
    public boolean boTriPhong(String maPhong, Integer maSinhVien) {
        Phong phong = phongRepository.findById(maPhong).orElse(null);
        if (phong == null || phong.getSoNguoiHienTai() >= phong.getSucChua() || phong.getTrangThai().equals("Đang sửa chữa")) {
            return false; // Phòng không hợp lệ hoặc đã đầy
        }

        SinhVien sv = sinhVienRepository.findById(maSinhVien).orElse(null);
        if (sv == null) return false;

        // Tăng số người hiện tại
        phong.setSoNguoiHienTai(phong.getSoNguoiHienTai() + 1);

        // Cập nhật trạng thái
        if (phong.getSoNguoiHienTai() == phong.getSucChua()) {
            phong.setTrangThai("Đã đầy");
        } else {
            phong.setTrangThai("Còn chỗ");
        }
        phongRepository.save(phong);

        // Tạo bản ghi Phân bổ phòng mới
        PhanBoPhong phanBoMoi = new PhanBoPhong();
        phanBoMoi.setPhong(phong);

        phanBoMoi.setSinhVien(sv);

        phanBoMoi.setNgayVao(LocalDate.now());
        phanBoMoi.setTrangThai("Đang lưu trú");
        phanBoPhongRepository.save(phanBoMoi);

        return true;
    }

    // 2. Nghiệp vụ Trả phòng
    @Transactional
    public boolean traPhong(Integer maPhanBo) {
        PhanBoPhong phanBo = phanBoPhongRepository.findById(maPhanBo).orElse(null);
        if (phanBo == null || !phanBo.getTrangThai().equals("Đang lưu trú")) {
            return false;
        }

        // Cập nhật ngày ra và trạng thái phân bổ
        phanBo.setNgayRa(LocalDate.now());
        phanBo.setTrangThai("Đã trả phòng");
        phanBoPhongRepository.save(phanBo);

        // Giảm số người trong phòng và cập nhật trạng thái phòng
        Phong phong = phanBo.getPhong();
        if (phong.getSoNguoiHienTai() > 0) {
            phong.setSoNguoiHienTai(phong.getSoNguoiHienTai() - 1);
        }

        if (phong.getSoNguoiHienTai() == 0) {
            phong.setTrangThai("Trống");
        } else {
            phong.setTrangThai("Còn chỗ");
        }
        phongRepository.save(phong);

        return true;
    }

    // 3. Nghiệp vụ Chuyển phòng
    @Transactional
    public boolean chuyenPhong(Integer maPhanBoCu, String maPhongMoi) {
        PhanBoPhong phanBoCu = phanBoPhongRepository.findById(maPhanBoCu).orElse(null);
        if (phanBoCu == null) return false;

        Integer maSinhVien = phanBoCu.getSinhVien().getMaSinhVien();

        // Bước 1: Trả phòng cũ
        boolean traPhongThanhCong = traPhong(maPhanBoCu);
        if (!traPhongThanhCong) return false;

        // Bước 2: Bố trí vào phòng mới
        return boTriPhong(maPhongMoi, maSinhVien);
    }
}
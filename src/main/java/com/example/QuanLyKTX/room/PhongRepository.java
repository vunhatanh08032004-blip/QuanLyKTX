package com.example.QuanLyKTX.room;

import com.example.QuanLyKTX.entity.Phong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongRepository extends JpaRepository<Phong, String> {
    // Có thể thêm các hàm tìm kiếm tùy chỉnh ở đây nếu cần, ví dụ tìm phòng theo tòa nhà
}
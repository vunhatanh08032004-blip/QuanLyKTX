package com.example.QuanLyKTX.room;

import com.example.QuanLyKTX.entity.PhanBoPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhanBoPhongRepository extends JpaRepository<PhanBoPhong, Integer> {
}
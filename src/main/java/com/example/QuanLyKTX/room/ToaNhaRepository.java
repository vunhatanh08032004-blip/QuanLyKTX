package com.example.QuanLyKTX.room;

import com.example.QuanLyKTX.entity.ToaNha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToaNhaRepository extends JpaRepository<ToaNha, String> {
}
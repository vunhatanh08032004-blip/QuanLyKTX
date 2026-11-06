package com.example.QuanLyKTX.maintenance;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/** Cho phép ảnh minh họa tối đa 5 MB mà không sửa application.properties dùng chung. */
@Configuration
public class MaintenanceUploadConfiguration {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(5));
        factory.setMaxRequestSize(DataSize.ofMegabytes(6));
        return factory.createMultipartConfig();
    }
}

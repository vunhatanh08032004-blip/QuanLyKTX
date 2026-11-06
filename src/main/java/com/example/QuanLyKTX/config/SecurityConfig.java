package com.example.QuanLyKTX.config;

import com.example.QuanLyKTX.common.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new AppPasswordEncoder();
    }

    /** Sai mật khẩu -> /login?error ; tài khoản bị khóa -> /login?locked */
    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String target = (exception instanceof LockedException) ? "/login?locked" : "/login?error";
            response.sendRedirect(request.getContextPath() + target);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh + trang công khai
                .requestMatchers("/css/**", "/js/**", "/images/**", "/error", "/403").permitAll()
                .requestMatchers("/login", "/register", "/forgot-password", "/reset-password").permitAll()

                // Phân quyền theo vai trò. QUY TẮC CỤ THỂ ĐẶT TRƯỚC (vd violation trước maintenance).
                // Phải khớp với bảng menu trong common/MenuBuilder.java
                .requestMatchers("/staff/**").hasRole(Roles.BAN_QUAN_LY)
                // Sinh viên: cả hai xem; khóa/mở khóa tài khoản (POST) chỉ Ban quản lý
                .requestMatchers(HttpMethod.POST, "/student/**").hasRole(Roles.BAN_QUAN_LY)
                .requestMatchers("/student/**").hasAnyRole(Roles.BAN_QUAN_LY, Roles.NV_QUAN_LY)
                .requestMatchers("/room/**").hasAnyRole(Roles.SINH_VIEN, Roles.NV_QUAN_LY, Roles.BAN_QUAN_LY)
                .requestMatchers("/registration/**").hasAnyRole(Roles.SINH_VIEN, Roles.NV_QUAN_LY, Roles.BAN_QUAN_LY)
                // Hợp đồng: sinh viên chỉ vào trang xem hợp đồng của mình (GET /contract) và trang gửi yêu cầu;
                // mọi chức năng quản lý còn lại chỉ NV quản lý + Ban quản lý.
                .requestMatchers("/contract/request/**").hasRole(Roles.SINH_VIEN)
                .requestMatchers(HttpMethod.GET, "/contract").hasAnyRole(Roles.SINH_VIEN, Roles.NV_QUAN_LY, Roles.BAN_QUAN_LY)
                .requestMatchers("/contract/**").hasAnyRole(Roles.NV_QUAN_LY, Roles.BAN_QUAN_LY)
                .requestMatchers("/payment/**").hasAnyRole(Roles.SINH_VIEN, Roles.NV_THU_PHI, Roles.BAN_QUAN_LY)
                .requestMatchers("/maintenance/violation/**").hasAnyRole(Roles.SINH_VIEN, Roles.NV_QUAN_LY, Roles.BAN_QUAN_LY)
                .requestMatchers("/maintenance/**").hasAnyRole(Roles.SINH_VIEN, Roles.NV_QUAN_LY, Roles.NV_BAO_TRI, Roles.BAN_QUAN_LY)
                .requestMatchers("/report/**").hasAnyRole(Roles.NV_THU_PHI, Roles.BAN_QUAN_LY)
                // /home, /account: mọi tài khoản đã đăng nhập
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .failureHandler(loginFailureHandler())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")                 // gửi bằng POST (có CSRF token)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}

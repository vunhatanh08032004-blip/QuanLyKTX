package com.example.QuanLyKTX.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.QuanLyKTX.common.Roles.*;

/**
 * Menu sidebar theo TỪNG vai trò: mỗi vai trò chỉ thấy chức năng thuộc thân phận của mình,
 * và cùng một địa chỉ có thể mang tên khác nhau (VD /room: "Phòng của tôi" với sinh viên,
 * "Quản lý phòng" với nhân viên quản lý).
 * Sửa bảng DEFS bên dưới thì nhớ sửa luôn quy tắc URL tương ứng trong SecurityConfig.
 */
public final class MenuBuilder {

    private record Entry(String label, String description) {}

    /** byRole: vai trò nào có trong map thì thấy mục này, kèm tên + mô tả riêng. */
    private record Def(String url, String icon, Map<String, Entry> byRole) {}

    private static Entry e(String label, String description) { return new Entry(label, description); }

    private static final List<Def> DEFS = List.of(
        new Def("/home", "i-home", Map.of(
            BAN_QUAN_LY, e("Trang chủ", ""), NV_QUAN_LY, e("Trang chủ", ""), NV_THU_PHI, e("Trang chủ", ""),
            NV_BAO_TRI, e("Trang chủ", ""), SINH_VIEN, e("Trang chủ", ""))),

        // ---- chỉ Ban quản lý (admin)
        new Def("/staff", "i-users", Map.of(
            BAN_QUAN_LY, e("Quản lý nhân viên", "Thêm nhân viên, phân vai trò, đặt lại mật khẩu, khóa/mở khóa tài khoản"))),

        // ---- Ban quản lý + Nhân viên quản lý
        new Def("/student", "i-user", Map.of(
            BAN_QUAN_LY, e("Quản lý sinh viên", "Tra cứu hồ sơ sinh viên, khóa/mở khóa tài khoản"),
            NV_QUAN_LY,  e("Quản lý sinh viên", "Tra cứu hồ sơ sinh viên đang đăng ký và lưu trú"))),

        new Def("/registration", "i-clipboard", Map.of(
            SINH_VIEN,  e("Đăng ký KTX", "Gửi đơn đăng ký lưu trú và theo dõi kết quả xét duyệt"),
            NV_QUAN_LY, e("Duyệt đăng ký", "Xét duyệt đơn đăng ký ở KTX của sinh viên"),
            BAN_QUAN_LY, e("Duyệt đăng ký", "Xét duyệt hoặc từ chối đơn đăng ký ở KTX, quyết định các trường hợp đặc biệt"))),

        new Def("/room", "i-building", Map.of(
            SINH_VIEN,  e("Phòng của tôi", "Xem phòng đang ở, bạn cùng phòng và tiện nghi"),
            NV_QUAN_LY, e("Quản lý phòng", "Quản lý danh sách phòng, xếp phòng, theo dõi chỗ trống"),
            BAN_QUAN_LY, e("Quản lý phòng", "Quản lý phòng, sức chứa, giá phòng và tình trạng sử dụng"))),

        new Def("/contract", "i-file", Map.of(
            SINH_VIEN,  e("Hợp đồng của tôi", "Xem hợp đồng lưu trú của bạn: phòng, thời hạn, giá"),
            NV_QUAN_LY, e("Quản lý hợp đồng", "Lập, gia hạn, thanh lý hợp đồng lưu trú"),
            BAN_QUAN_LY, e("Quản lý hợp đồng", "Phê duyệt, gia hạn, thanh lý hợp đồng lưu trú"))),

        // Sinh viên chọn: gia hạn hợp đồng / hủy hợp đồng (không thuê nữa) / chuyển phòng
        new Def("/contract/request", "i-clipboard", Map.of(
            SINH_VIEN,  e("Yêu cầu hợp đồng", "Gửi yêu cầu gia hạn hợp đồng, hủy hợp đồng (không thuê nữa) hoặc chuyển phòng"))),

        new Def("/payment", "i-wallet", Map.of(
            SINH_VIEN,  e("Hóa đơn của tôi", "Xem hóa đơn, thanh toán và lịch sử đóng phí"),
            NV_THU_PHI, e("Thu phí", "Lập hóa đơn, ghi nhận thanh toán, theo dõi công nợ"),
            BAN_QUAN_LY, e("Thu phí - Thanh toán", "Duyệt và điều chỉnh hóa đơn, đối soát tình hình thu phí"))),

        new Def("/maintenance", "i-chat", Map.of(
            SINH_VIEN,  e("Gửi phản ánh", "Gửi yêu cầu sửa chữa/phản ánh và xem tiến độ xử lý"),
            NV_QUAN_LY, e("Tiếp nhận phản ánh", "Tiếp nhận phản ánh của sinh viên và chuyển bộ phận bảo trì"),
            NV_BAO_TRI, e("Yêu cầu sửa chữa", "Nhận việc, cập nhật tiến độ và hoàn thành sửa chữa"),
            BAN_QUAN_LY, e("Phản ánh - Bảo trì", "Duyệt, phân công yêu cầu sửa chữa và theo dõi tiến độ"))),

        new Def("/maintenance/violation", "i-alert", Map.of(
            SINH_VIEN,  e("Vi phạm của tôi", "Xem các vi phạm nội quy của bản thân"),
            NV_QUAN_LY, e("Vi phạm nội quy", "Ghi nhận và xử lý vi phạm nội quy"),
            BAN_QUAN_LY, e("Vi phạm nội quy", "Duyệt biên bản và quyết định hình thức xử lý"))),

        new Def("/report", "i-chart", Map.of(
            NV_THU_PHI, e("Báo cáo thu phí", "Thống kê doanh thu và công nợ"),
            BAN_QUAN_LY, e("Báo cáo - Thống kê", "Báo cáo tổng hợp toàn hệ thống"))),

        new Def("/account", "i-user", Map.of(
            BAN_QUAN_LY, e("Tài khoản", "Xem thông tin tài khoản"), NV_QUAN_LY, e("Tài khoản", "Xem thông tin tài khoản"),
            NV_THU_PHI, e("Tài khoản", "Xem thông tin tài khoản"), NV_BAO_TRI, e("Tài khoản", "Xem thông tin tài khoản"),
            SINH_VIEN, e("Tài khoản", "Xem thông tin cá nhân")))
    );

    private MenuBuilder() {}

    public static List<MenuItem> forRole(String role, String requestPath) {
        // Mục "active" là mục có URL khớp dài nhất (để /maintenance/violation không sáng cả /maintenance)
        String activeUrl = null;
        for (Def d : DEFS) {
            if (d.byRole().containsKey(role) && matches(requestPath, d.url())
                    && (activeUrl == null || d.url().length() > activeUrl.length())) {
                activeUrl = d.url();
            }
        }
        List<MenuItem> items = new ArrayList<>();
        for (Def d : DEFS) {
            Entry en = d.byRole().get(role);
            if (en != null) {
                items.add(new MenuItem(en.label(), d.url(), d.icon(), d.url().equals(activeUrl), en.description()));
            }
        }
        return items;
    }

    private static boolean matches(String path, String url) {
        return path.equals(url) || path.startsWith(url + "/");
    }
}

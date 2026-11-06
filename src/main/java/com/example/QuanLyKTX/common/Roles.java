package com.example.QuanLyKTX.common;

import java.util.Map;

/** Tên 5 vai trò – trùng với cột VaiTro.TenVaiTro trong database. */
public final class Roles {

    public static final String BAN_QUAN_LY       = "BAN_QUAN_LY_KTX";
    public static final String NV_QUAN_LY        = "NHAN_VIEN_QUAN_LY_KTX";
    public static final String NV_THU_PHI        = "NHAN_VIEN_THU_PHI";
    public static final String NV_BAO_TRI        = "NHAN_VIEN_BAO_TRI";
    public static final String SINH_VIEN         = "SINH_VIEN";

    private static final Map<String, String> LABELS = Map.of(
            BAN_QUAN_LY, "Ban quản lý KTX",
            NV_QUAN_LY,  "Nhân viên quản lý KTX",
            NV_THU_PHI,  "Nhân viên thu phí",
            NV_BAO_TRI,  "Nhân viên bảo trì",
            SINH_VIEN,   "Sinh viên");

    private static final Map<String, String> INTROS = Map.of(
            BAN_QUAN_LY, "Quản trị toàn hệ thống: nhân viên, sinh viên, phòng, hợp đồng, thu phí và báo cáo.",
            NV_QUAN_LY,  "Quản lý sinh viên, duyệt đăng ký, xếp phòng, hợp đồng và xử lý vi phạm nội quy.",
            NV_THU_PHI,  "Lập hóa đơn, ghi nhận thanh toán, theo dõi công nợ và báo cáo thu phí.",
            NV_BAO_TRI,  "Tiếp nhận yêu cầu sửa chữa, cập nhật tiến độ và hoàn thành bảo trì.",
            SINH_VIEN,   "Đăng ký ở ký túc xá, xem phòng và hợp đồng, yêu cầu gia hạn/hủy/chuyển phòng, xem hóa đơn và gửi phản ánh.");

    private Roles() {}

    /** Tên hiển thị tiếng Việt của vai trò. */
    public static String label(String role) {
        return LABELS.getOrDefault(role, role);
    }

    /** Mô tả ngắn công việc của vai trò, hiện ở trang chủ. */
    public static String intro(String role) {
        return INTROS.getOrDefault(role, "");
    }
}

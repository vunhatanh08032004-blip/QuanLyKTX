package com.example.QuanLyKTX.maintenance;

/** Nhãn hiển thị; giá trị lưu DB giữ đúng mã theo QuanLyKTX.sql. */
public final class MaintenanceLabels {
    private MaintenanceLabels() {}
    public static String feedback(String s) {
        if(s==null)return "Chưa rõ";
        return switch(s){
            case "MOI_GUI" -> "Mới gửi";
            case "DANG_TIEP_NHAN" -> "Đang tiếp nhận";
            case "DA_PHAN_CONG" -> "Đã phân công";
            case "DANG_XU_LY" -> "Đang xử lý";
            case "CHO_NGHIEM_THU" -> "Chờ nghiệm thu";
            case "HOAN_TAT" -> "Hoàn tất";
            case "TU_CHOI" -> "Từ chối";
            default -> s;
        };
    }
    public static String severity(String s) {
        if(s==null)return "Chưa phân loại";
        return switch(s){
            case "THAP" -> "Thấp";
            case "TRUNG_BINH" -> "Trung bình";
            case "CAO" -> "Cao";
            case "KHAN_CAP" -> "Khẩn cấp";
            default -> s;
        };
    }
    public static String approval(String s) {
        if(s==null)return "Chưa có";
        return switch(s){
            case "KHONG_CAN" -> "Không cần duyệt";
            case "CHO_DUYET" -> "Chờ duyệt";
            case "DA_DUYET" -> "Đã duyệt";
            case "TU_CHOI" -> "Không duyệt";
            default -> s;
        };
    }
    public static String violation(String s) {
        if(s==null)return "Chưa rõ";
        return switch(s){
            case "MOI_LAP" -> "Mới lập";
            case "CHO_XEM_XET" -> "Chờ xem xét";
            case "DA_XU_LY" -> "Đã xử lý";
            default -> s;
        };
    }
    public static String violationSeverity(String s) {
        if(s==null)return "Chưa rõ";
        return switch(s){
            case "NHE" -> "Nhẹ";
            case "TRUNG_BINH" -> "Trung bình";
            case "NGHIEM_TRONG" -> "Nghiêm trọng";
            default -> s;
        };
    }
}

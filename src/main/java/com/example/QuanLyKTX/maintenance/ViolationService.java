package com.example.QuanLyKTX.maintenance;

import com.example.QuanLyKTX.common.Roles;
import com.example.QuanLyKTX.config.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class ViolationService {
    private final ViolationRepository repo;
    private final MaintenanceRepository maintenance;
    public ViolationService(ViolationRepository repo,MaintenanceRepository maintenance){
        this.repo=repo;this.maintenance=maintenance;
    }
    public boolean admin(CustomUserDetails u){return Roles.BAN_QUAN_LY.equals(u.getVaiTro());}
    public boolean manager(CustomUserDetails u){return Roles.NV_QUAN_LY.equals(u.getVaiTro());}
    public boolean student(CustomUserDetails u){return Roles.SINH_VIEN.equals(u.getVaiTro());}
    public void require(boolean yes){if(!yes)throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
    public List<ViolationRepository.Violation> search(CustomUserDetails u,String q,String status,
                                                       LocalDate from,LocalDate to,String severity){
        if(from!=null&&to!=null&&from.isAfter(to))
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        if(severity!=null&&!severity.isBlank()&&!List.of("NHE","TRUNG_BINH","NGHIEM_TRONG").contains(severity))
            throw new IllegalArgumentException("Mức độ lọc không hợp lệ");
        if(student(u))return repo.search(q,status,maintenance.studentIdForAccount(u.getMaTaiKhoan()).orElse(-1),from,to,severity);
        require(admin(u)||manager(u));return repo.search(q,status,null,from,to,severity);
    }
    public ViolationRepository.Violation visible(int id,CustomUserDetails u){
        var v=repo.find(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(student(u))require(maintenance.studentIdForAccount(u.getMaTaiKhoan()).filter(v.studentId()::equals).isPresent());
        else require(admin(u)||manager(u));
        return v;
    }
    public List<MaintenanceRepository.Option> students(CustomUserDetails u){
        require(manager(u));return maintenance.students();
    }
    public List<MaintenanceRepository.RoomOption> rooms(int studentId,CustomUserDetails u){
        require(manager(u));return maintenance.studentRooms(studentId);
    }
    @Transactional
    public int create(CustomUserDetails u,int studentId,String room,LocalDateTime when,
                      String place,String description,String severity){
        require(manager(u));
        if(!maintenance.studentExists(studentId))throw new IllegalArgumentException("Sinh viên không tồn tại");
        if(room!=null&&!room.isBlank()&&!maintenance.roomBelongsToStudent(studentId,room.trim()))
            throw new IllegalArgumentException("Phòng được chọn không thuộc sinh viên này");
        if(when==null||when.isAfter(LocalDateTime.now().plusMinutes(5)))
            throw new IllegalArgumentException("Thời điểm vi phạm không hợp lệ");
        required(place,200,"Địa điểm");required(description,10000,"Nội dung");
        if(!List.of("NHE","TRUNG_BINH","NGHIEM_TRONG").contains(severity))
            throw new IllegalArgumentException("Mức độ vi phạm không hợp lệ");
        return repo.create(studentId,room==null||room.isBlank()?null:room.trim(),u.getMaTaiKhoan(),
            when,place.trim(),description.trim(),severity);
    }
    @Transactional
    public void review(int id,CustomUserDetails u){
        require(manager(u));var v=visible(id,u);
        if(v.resolution()!=null||!"MOI_LAP".equals(v.status()))
            throw new IllegalArgumentException("Biên bản đã được trình hoặc xử lý");
        repo.requestReview(id);
    }
    @Transactional
    public void decide(int id,CustomUserDetails u,String method,BigDecimal fine,String note){
        require(admin(u));var v=visible(id,u);
        if(v.resolution()!=null||"DA_XU_LY".equals(v.status()))
            throw new IllegalArgumentException("Biên bản đã có quyết định xử lý");
        required(method,100,"Hình thức xử lý");
        if(fine!=null&&(fine.signum()<0||fine.scale()>2||fine.compareTo(new BigDecimal("9999999999.99"))>0))
            throw new IllegalArgumentException("Số tiền phạt không hợp lệ");
        if(fine!=null&&fine.signum()==0)fine=null;
        if("Phạt tiền".equalsIgnoreCase(method.trim())&&fine==null)
            throw new IllegalArgumentException("Cần nhập số tiền cho hình thức phạt tiền");
        if(note!=null&&note.length()>500)throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự");
        repo.decide(id,u.getMaTaiKhoan(),method.trim(),fine,note==null?null:note.trim());
        // Không tự tạo hóa đơn/điều chỉnh công nợ thuộc module của Thúy.
    }
    private void required(String value,int length,String name){
        if(value==null||value.isBlank()||value.trim().length()>length)
            throw new IllegalArgumentException(name+" bắt buộc và tối đa "+length+" ký tự");
    }
}

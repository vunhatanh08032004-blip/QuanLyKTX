package com.example.QuanLyKTX.maintenance;

import com.example.QuanLyKTX.common.Roles;
import com.example.QuanLyKTX.config.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceService {
    private final MaintenanceRepository repo;
    private final Path uploadDirectory = Path.of("uploads", "maintenance").toAbsolutePath().normalize();
    public MaintenanceService(MaintenanceRepository repo) { this.repo=repo; }

    public boolean isStudent(CustomUserDetails u) { return Roles.SINH_VIEN.equals(u.getVaiTro()); }
    public boolean isManager(CustomUserDetails u) { return Roles.NV_QUAN_LY.equals(u.getVaiTro()); }
    public boolean isAdmin(CustomUserDetails u) { return Roles.BAN_QUAN_LY.equals(u.getVaiTro()); }
    public boolean isTechnician(CustomUserDetails u) { return Roles.NV_BAO_TRI.equals(u.getVaiTro()); }
    public boolean canManage(CustomUserDetails u) { return isAdmin(u)||isManager(u); }

    public void requireRole(boolean allowed) {
        if(!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Bạn không có quyền thực hiện thao tác này");
    }
    private int studentId(CustomUserDetails u) {
        return repo.studentIdForAccount(u.getMaTaiKhoan())
            .orElseThrow(()->new IllegalArgumentException("Tài khoản chưa có hồ sơ sinh viên"));
    }
    public List<MaintenanceRepository.Feedback> search(CustomUserDetails u,String q,String status) {
        if(isStudent(u)) return repo.search(q,status,repo.studentIdForAccount(u.getMaTaiKhoan()).orElse(-1),null);
        if(isTechnician(u)) return repo.search(q,status,null,u.getMaTaiKhoan());
        requireRole(canManage(u));
        return repo.search(q,status,null,null);
    }
    public List<MaintenanceRepository.RoomOption> myRooms(CustomUserDetails u) {
        requireRole(isStudent(u));
        return repo.studentIdForAccount(u.getMaTaiKhoan()).map(repo::studentRooms).orElseGet(List::of);
    }
    public List<MaintenanceRepository.Option> technicians(CustomUserDetails u){
        requireRole(canManage(u));return repo.technicians();
    }
    public MaintenanceRepository.Feedback visible(int id,CustomUserDetails u) {
        var f=repo.find(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có phản ánh"));
        if(isStudent(u)) requireRole(repo.studentIdForAccount(u.getMaTaiKhoan()).filter(f.studentId()::equals).isPresent());
        else if(isTechnician(u)) requireRole(u.getMaTaiKhoan().equals(f.technicianId()));
        else requireRole(canManage(u));
        return f;
    }
    public List<MaintenanceRepository.Log> history(int id,CustomUserDetails u){ visible(id,u);return repo.history(id); }
    public List<MaintenanceRepository.Material> materials(int id,CustomUserDetails u){
        var f=visible(id,u);
        return f.assignmentId()==null?List.of():repo.materials(f.assignmentId());
    }
    public List<MaintenanceRepository.Attachment> attachments(int id,CustomUserDetails u){
        visible(id,u);return repo.attachments(id);
    }
    @Transactional
    public int submit(CustomUserDetails u,String room,String equipment,String title,String description,
                      LocalDateTime happened,String severity,MultipartFile photo){
        requireRole(isStudent(u));
        int student=studentId(u);
        required(room,20,"Phòng"); required(title,200,"Tiêu đề");required(description,10000,"Mô tả sự cố");
        optional(equipment,100,"Tên thiết bị");
        if(!repo.roomBelongsToStudent(student,room))
            throw new IllegalArgumentException("Chỉ được gửi phản ánh cho phòng bạn đang ở");
        if(!List.of("THAP","TRUNG_BINH","CAO","KHAN_CAP").contains(severity))
            throw new IllegalArgumentException("Mức độ không hợp lệ");
        if(happened!=null && happened.isAfter(LocalDateTime.now().plusMinutes(5)))
            throw new IllegalArgumentException("Thời gian sự cố không được ở tương lai");
        // Kiểm tra ảnh trước khi ghi dữ liệu để lỗi định dạng không tạo phản ánh rác.
        byte[] bytes=readPhoto(photo);
        String extension=photoExtension(bytes);
        int id=repo.create(student,room,trimToNull(equipment),title.trim(),description.trim(),happened,severity);
        if(bytes!=null){
            String filename=UUID.randomUUID()+"."+extension;
            try {
                Files.createDirectories(uploadDirectory);
                Files.write(uploadDirectory.resolve(filename),bytes,StandardOpenOption.CREATE_NEW);
            } catch(IOException ex){
                throw new IllegalStateException("Không lưu được ảnh đính kèm",ex);
            }
            repo.saveAttachment(id,"uploads/maintenance/"+filename);
        }
        repo.log(id,null,null,"MOI_GUI","Sinh viên đã gửi phản ánh",u.getMaTaiKhoan());
        return id;
    }
    public record Photo(byte[] content,String mediaType) {}
    public Photo photo(int attachmentId,CustomUserDetails u){
        var a=repo.attachment(attachmentId)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        visible(a.feedbackId(),u);
        // Ảnh mẫu cũ trong SQL có thể chưa tồn tại; chỉ phục vụ ảnh upload của module này.
        String path=a.path();
        if(!path.matches("uploads/maintenance/[0-9a-fA-F-]{36}\\.(jpg|png|webp)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Ảnh mẫu không có trên máy chủ");
        Path resolved=Path.of(path).toAbsolutePath().normalize();
        if(!resolved.startsWith(uploadDirectory))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        try {
            byte[] bytes=Files.readAllBytes(resolved);
            String type=path.endsWith(".png")?"image/png":path.endsWith(".webp")?"image/webp":"image/jpeg";
            return new Photo(bytes,type);
        } catch(IOException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy ảnh");}
    }
    @Transactional
    public void receive(int id,CustomUserDetails u){
        requireRole(canManage(u));var f=visible(id,u);atStatus(f,"MOI_GUI");
        repo.receive(id,u.getMaTaiKhoan());log(f,"DANG_TIEP_NHAN","Đã tiếp nhận",u);
    }
    @Transactional
    public void reject(int id,CustomUserDetails u,String reason){
        requireRole(canManage(u));var f=visible(id,u);
        if(!List.of("MOI_GUI","DANG_TIEP_NHAN").contains(f.status())||f.assignmentId()!=null)
            throw new IllegalArgumentException("Không thể từ chối yêu cầu đã phân công");
        required(reason,500,"Lý do từ chối");
        repo.reject(id,u.getMaTaiKhoan(),reason.trim());log(f,"TU_CHOI",reason.trim(),u);
    }
    @Transactional
    public void assign(int id,CustomUserDetails u,int techId,LocalDateTime due){
        requireRole(canManage(u));var f=visible(id,u);
        if(!List.of("MOI_GUI","DANG_TIEP_NHAN","DA_PHAN_CONG").contains(f.status()))
            throw new IllegalArgumentException("Trạng thái hiện tại không cho phép phân công");
        if(!repo.isTechnician(techId))throw new IllegalArgumentException("Hãy chọn nhân viên bảo trì đang hoạt động");
        if(due!=null && due.isBefore(LocalDateTime.now()))throw new IllegalArgumentException("Hạn xử lý phải ở tương lai");
        int assignment=repo.assign(id,techId,u.getMaTaiKhoan(),due);
        repo.log(id,assignment,f.status(),"DA_PHAN_CONG","Đã phân công kỹ thuật viên #"+techId,u.getMaTaiKhoan());
    }
    private MaintenanceRepository.Feedback assigned(int id,CustomUserDetails u){
        requireRole(isTechnician(u));var f=visible(id,u);
        if(f.assignmentId()==null)throw new IllegalArgumentException("Phản ánh chưa có phân công");
        return f;
    }
    @Transactional
    public void start(int id,CustomUserDetails u){
        var f=assigned(id,u);
        if(!List.of("DA_PHAN_CONG","MOI_GUI","DANG_TIEP_NHAN").contains(f.status()))
            throw new IllegalArgumentException("Yêu cầu không ở trạng thái chờ xử lý");
        repo.setFeedbackStatus(id,"DANG_XU_LY");log(f,"DANG_XU_LY","Kỹ thuật viên bắt đầu xử lý",u);
    }
    @Transactional
    public void addMaterial(int id,CustomUserDetails u,String name,int quantity,BigDecimal price){
        var f=assigned(id,u);atStatus(f,"DANG_XU_LY");
        required(name,150,"Tên vật tư");
        if(quantity<1||quantity>10000)throw new IllegalArgumentException("Số lượng vật tư không hợp lệ");
        if(price==null||price.signum()<0||price.scale()>2||price.compareTo(new BigDecimal("1000000000"))>0)
            throw new IllegalArgumentException("Đơn giá phải không âm và có tối đa hai số thập phân");
        if(price.signum()==0)throw new IllegalArgumentException("Đơn giá vật tư phải lớn hơn 0");
        repo.addMaterial(f.assignmentId(),name.trim(),quantity,price);
        repo.log(id,f.assignmentId(),f.status(),f.status(),"Đã ghi vật tư: "+name.trim(),u.getMaTaiKhoan());
    }
    @Transactional
    public void removeMaterial(int id,CustomUserDetails u,int materialId){
        var f=assigned(id,u);atStatus(f,"DANG_XU_LY");
        if(!repo.removeMaterial(f.assignmentId(),materialId))
            throw new IllegalArgumentException("Không tìm thấy vật tư cần xóa");
        repo.log(id,f.assignmentId(),f.status(),f.status(),"Đã chỉnh sửa danh sách vật tư",u.getMaTaiKhoan());
    }
    @Transactional
    public void result(int id,CustomUserDetails u,String inspection,String repair){
        var f=assigned(id,u);atStatus(f,"DANG_XU_LY");
        required(inspection,10000,"Kết quả kiểm tra");required(repair,10000,"Nội dung sửa chữa");
        repo.updateRepair(f.assignmentId(),inspection.trim(),repair.trim());
        repo.setFeedbackStatus(id,"CHO_NGHIEM_THU");
        log(f,"CHO_NGHIEM_THU","Đã báo cáo hoàn thành, chờ nghiệm thu",u);
    }
    @Transactional
    public void approve(int id,CustomUserDetails u,boolean approve,String note){
        requireRole(isAdmin(u));var f=visible(id,u);
        if(f.assignmentId()==null||!"CHO_DUYET".equals(f.approvalStatus()))
            throw new IllegalArgumentException("Phân công này không có chi phí chờ duyệt");
        if(!approve)required(note,500,"Lý do không duyệt");
        optional(note,500,"Ghi chú");
        repo.decision(f.assignmentId(),u.getMaTaiKhoan(),approve,trimToNull(note));
        if(!approve)repo.setFeedbackStatus(id,"DANG_XU_LY");
        repo.log(id,f.assignmentId(),f.status(),approve?f.status():"DANG_XU_LY",
            approve?"Ban quản lý đã duyệt chi phí":"Chi phí không được duyệt: "+note.trim(),u.getMaTaiKhoan());
    }
    @Transactional
    public void finish(int id,CustomUserDetails u){
        requireRole(canManage(u));var f=visible(id,u);atStatus(f,"CHO_NGHIEM_THU");
        if(f.assignmentId()==null||f.repair()==null||f.repair().isBlank())
            throw new IllegalArgumentException("Chưa có kết quả sửa chữa");
        if(f.cost()!=null && f.cost().signum()>0
           && !"DA_DUYET".equals(f.approvalStatus()) && !"KHONG_CAN".equals(f.approvalStatus()))
            throw new IllegalArgumentException("Ban quản lý phải duyệt chi phí đang chờ trước khi nghiệm thu");
        repo.complete(f.assignmentId());repo.setFeedbackStatus(id,"HOAN_TAT");
        log(f,"HOAN_TAT","Đã nghiệm thu và hoàn tất phản ánh",u);
    }
    private void log(MaintenanceRepository.Feedback f,String next,String text,CustomUserDetails u){
        repo.log(f.id(),f.assignmentId(),f.status(),next,text,u.getMaTaiKhoan());
    }
    private static void atStatus(MaintenanceRepository.Feedback f,String state){
        if(!state.equals(f.status()))throw new IllegalArgumentException("Thao tác không phù hợp với trạng thái hiện tại");
    }
    private static void required(String text,int max,String name){
        if(text==null||text.isBlank()||text.trim().length()>max)
            throw new IllegalArgumentException(name+" phải có từ 1 đến "+max+" ký tự");
    }
    private static void optional(String text,int max,String name){
        if(text!=null&&text.trim().length()>max)throw new IllegalArgumentException(name+" quá dài");
    }
    private static String trimToNull(String value){
        return value==null||value.isBlank()?null:value.trim();
    }
    private static byte[] readPhoto(MultipartFile file){
        if(file==null||file.isEmpty())return null;
        if(file.getSize()>5*1024*1024)throw new IllegalArgumentException("Ảnh phải nhỏ hơn hoặc bằng 5 MB");
        try{
            byte[] bytes=file.getBytes();
            if(photoExtension(bytes)==null)throw new IllegalArgumentException("Chỉ nhận ảnh JPG, PNG, WEBP");
            return bytes;
        }catch(IOException e){throw new IllegalArgumentException("Không đọc được ảnh",e);}
    }
    private static String photoExtension(byte[] bytes){
        if(bytes==null)return null;
        if(bytes.length>=3&&(bytes[0]&255)==255&&(bytes[1]&255)==216&&(bytes[2]&255)==255)return "jpg";
        if(bytes.length>=8&&(bytes[0]&255)==137&&bytes[1]==80&&bytes[2]==78&&bytes[3]==71)return "png";
        if(bytes.length>=12&&bytes[0]==82&&bytes[1]==73&&bytes[2]==70&&bytes[3]==70&&
           bytes[8]==87&&bytes[9]==69&&bytes[10]==66&&bytes[11]==80)return "webp";
        return null;
    }
}

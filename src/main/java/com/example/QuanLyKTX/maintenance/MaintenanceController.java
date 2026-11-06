package com.example.QuanLyKTX.maintenance;

import com.example.QuanLyKTX.config.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Chỉ xử lý /maintenance, không đụng vào controller hoặc SecurityConfig của Bình. */
@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {
    private final MaintenanceService service;
    public MaintenanceController(MaintenanceService service){this.service=service;}

    @GetMapping({"", "/"})
    public String list(@AuthenticationPrincipal CustomUserDetails user,
                       @RequestParam(defaultValue="") String q,
                       @RequestParam(defaultValue="") String status, Model model){
        var feedbacks=service.search(user,q,status);
        model.addAttribute("feedbacks",feedbacks);
        model.addAttribute("q",q);model.addAttribute("status",status);
        model.addAttribute("canManage",service.canManage(user));
        model.addAttribute("isStudent",service.isStudent(user));
        model.addAttribute("isTechnician",service.isTechnician(user));
        model.addAttribute("countNew",feedbacks.stream().filter(f->"MOI_GUI".equals(f.status())).count());
        model.addAttribute("countDoing",feedbacks.stream().filter(f->"DANG_XU_LY".equals(f.status())).count());
        model.addAttribute("countDone",feedbacks.stream().filter(f->"HOAN_TAT".equals(f.status())).count());
        return "maintenance/feedback-list";
    }
    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal CustomUserDetails user,Model model){
        model.addAttribute("rooms",service.myRooms(user));
        return "maintenance/feedback-form";
    }
    @PostMapping("/new")
    public String submit(@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam String maPhong,
                         @RequestParam(defaultValue="") String tenThietBi,
                         @RequestParam String tieuDe,@RequestParam String moTa,
                         @RequestParam(defaultValue="TRUNG_BINH") String mucDo,
                         @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime thoiDiemXayRa,
                         @RequestParam(required=false,name="anh") MultipartFile photo,
                         RedirectAttributes flash){
        try{
            int id=service.submit(user,maPhong,tenThietBi,tieuDe,moTa,thoiDiemXayRa,mucDo,photo);
            flash.addFlashAttribute("success","Đã gửi phản ánh #"+id);
            return "redirect:/maintenance/"+id;
        }catch(IllegalArgumentException|IllegalStateException e){
            flash.addFlashAttribute("error",e.getMessage());return "redirect:/maintenance/new";
        }
    }
    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,Model model){
        var feedback=service.visible(id,user);
        model.addAttribute("f",feedback);
        model.addAttribute("history",service.history(id,user));
        model.addAttribute("materials",service.materials(id,user));
        model.addAttribute("attachments",service.attachments(id,user));
        model.addAttribute("canManage",service.canManage(user));
        model.addAttribute("isAdmin",service.isAdmin(user));
        model.addAttribute("isTechnician",service.isTechnician(user));
        model.addAttribute("isStudent",service.isStudent(user));
        model.addAttribute("technicians",service.canManage(user)?service.technicians(user):List.of());
        return "maintenance/feedback-detail";
    }
    @GetMapping("/attachment/{id:\\d+}")
    public ResponseEntity<byte[]> attachment(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user){
        var photo=service.photo(id,user);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .contentType(MediaType.parseMediaType(photo.mediaType()))
            .header("X-Content-Type-Options","nosniff").body(photo.content());
    }
    @PostMapping("/{id:\\d+}/receive")
    public String receive(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.receive(id,user),"Đã tiếp nhận phản ánh");
    }
    @PostMapping("/{id:\\d+}/reject")
    public String reject(@PathVariable int id,@RequestParam String reason,
                         @AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.reject(id,user,reason),"Đã từ chối phản ánh");
    }
    @PostMapping("/{id:\\d+}/assign")
    public String assign(@PathVariable int id,@RequestParam int technicianId,
                         @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime due,
                         @AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.assign(id,user,technicianId,due),"Đã phân công bảo trì");
    }
    @PostMapping("/{id:\\d+}/start")
    public String start(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.start(id,user),"Đã bắt đầu xử lý");
    }
    @PostMapping("/{id:\\d+}/material")
    public String material(@PathVariable int id,@RequestParam String name,
                           @RequestParam int quantity,@RequestParam BigDecimal price,
                           @AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.addMaterial(id,user,name,quantity,price),"Đã ghi nhận vật tư, chờ duyệt chi phí");
    }
    @PostMapping("/{id:\\d+}/material/{materialId:\\d+}/remove")
    public String removeMaterial(@PathVariable int id,@PathVariable int materialId,
                                 @AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.removeMaterial(id,user,materialId),"Đã chỉnh sửa danh sách vật tư");
    }
    @PostMapping("/{id:\\d+}/result")
    public String result(@PathVariable int id,@RequestParam String inspection,@RequestParam String repair,
                         @AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.result(id,user,inspection,repair),"Đã gửi kết quả, chờ nghiệm thu");
    }
    @PostMapping("/{id:\\d+}/approve")
    public String approve(@PathVariable int id,@RequestParam String verdict,
                          @RequestParam(defaultValue="") String note,
                          @AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->{
            if(!"APPROVE".equals(verdict)&&!"REJECT".equals(verdict))
                throw new IllegalArgumentException("Lựa chọn phê duyệt không hợp lệ");
            service.approve(id,user,"APPROVE".equals(verdict),note);
        },"Đã lưu quyết định chi phí");
    }
    @PostMapping("/{id:\\d+}/finish")
    public String finish(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        return action(id,ra,()->service.finish(id,user),"Đã nghiệm thu và hoàn tất");
    }
    private String action(int id,RedirectAttributes flash,Runnable run,String success){
        try{run.run();flash.addFlashAttribute("success",success);}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/maintenance/"+id;
    }
}

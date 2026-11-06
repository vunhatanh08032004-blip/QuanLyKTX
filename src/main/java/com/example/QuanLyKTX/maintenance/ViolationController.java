package com.example.QuanLyKTX.maintenance;

import com.example.QuanLyKTX.config.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.Collectors;
import java.util.TreeMap;

@Controller
@RequestMapping("/maintenance/violation")
public class ViolationController {
    private final ViolationService service;
    public ViolationController(ViolationService service){this.service=service;}

    @GetMapping({"", "/"})
    public String list(@AuthenticationPrincipal CustomUserDetails user,
                       @RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String status,
                       @RequestParam(defaultValue="") String severity,
                       @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model){
        var records=service.search(user,q,status,from,to,severity);
        model.addAttribute("violations",records);
        model.addAttribute("q",q);model.addAttribute("status",status);
        model.addAttribute("severity",severity); model.addAttribute("from",from);model.addAttribute("to",to);
        model.addAttribute("isManager",service.manager(user));
        model.addAttribute("isAdmin",service.admin(user));
        model.addAttribute("countPending",records.stream().filter(v->v.resolution()==null).count());
        model.addAttribute("countResolved",records.stream().filter(v->v.resolution()!=null).count());
        model.addAttribute("countSerious",records.stream().filter(v->"NGHIEM_TRONG".equals(v.severity())).count());
        model.addAttribute("countMinor",records.stream().filter(v->"NHE".equals(v.severity())).count());
        model.addAttribute("countModerate",records.stream().filter(v->"TRUNG_BINH".equals(v.severity())).count());
        var byMonth = records.stream().filter(v->v.happenedAt()!=null).collect(
            Collectors.groupingBy(v->YearMonth.from(v.happenedAt()).toString(),
                ()->new TreeMap<String,Long>(java.util.Comparator.reverseOrder()),Collectors.counting()));
        model.addAttribute("byMonth",byMonth);
        return "maintenance/violation-list";
    }
    @GetMapping("/new")
    public String form(@AuthenticationPrincipal CustomUserDetails user,Model model){
        model.addAttribute("students",service.students(user));
        return "maintenance/violation-form";
    }
    @PostMapping("/new")
    public String create(@AuthenticationPrincipal CustomUserDetails user,@RequestParam int studentId,
                         @RequestParam(defaultValue="") String room,
                         @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime when,
                         @RequestParam String place,@RequestParam String description,
                         @RequestParam String severity,RedirectAttributes ra){
        try{
            int id=service.create(user,studentId,room,when,place,description,severity);
            ra.addFlashAttribute("success","Đã lập biên bản #"+id);
            return "redirect:/maintenance/violation/"+id;
        }catch(IllegalArgumentException e){
            ra.addFlashAttribute("error",e.getMessage());return "redirect:/maintenance/violation/new";
        }
    }
    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,Model model){
        model.addAttribute("v",service.visible(id,user));
        model.addAttribute("isManager",service.manager(user));
        model.addAttribute("isAdmin",service.admin(user));
        return "maintenance/violation-detail";
    }
    @PostMapping("/{id:\\d+}/review")
    public String review(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,RedirectAttributes ra){
        try{service.review(id,user);ra.addFlashAttribute("success","Đã trình Ban quản lý xem xét");}
        catch(IllegalArgumentException e){ra.addFlashAttribute("error",e.getMessage());}
        return "redirect:/maintenance/violation/"+id;
    }
    @PostMapping("/{id:\\d+}/decide")
    public String decide(@PathVariable int id,@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam String method,@RequestParam(required=false) BigDecimal fine,
                         @RequestParam(defaultValue="") String note,RedirectAttributes ra){
        try{service.decide(id,user,method,fine,note);ra.addFlashAttribute("success","Đã ghi quyết định xử lý");}
        catch(IllegalArgumentException e){ra.addFlashAttribute("error",e.getMessage());}
        return "redirect:/maintenance/violation/"+id;
    }
}

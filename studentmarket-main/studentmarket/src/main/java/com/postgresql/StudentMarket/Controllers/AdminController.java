package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/login-admin")
    public String loginAdminPage() {
        return "admin/loginAdmin";  
    }

    @PostMapping("/login-admin")
    public String loginAdmin(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             Model model) {
        User admin = adminService.validateAdmin(username, password);
        if (admin != null) {
            // ✅ đăng nhập thành công → chuyển tới homepageAdmin
            return "redirect:/admin/homepageAdmin";
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu.");
            return "admin/loginAdmin"; 
        }
    }

    @GetMapping("/admin/homepageAdmin")
    public String adminHomepage() {
        return "admin/homepageAdmin"; // trả về view homepageAdmin.html
    }
}

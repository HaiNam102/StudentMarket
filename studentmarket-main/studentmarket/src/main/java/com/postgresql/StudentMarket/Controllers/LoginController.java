package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.Product;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.ProductRepository;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.CategoryMenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryMenuService categoryMenuService;

    /** ---------- Helpers ---------- */

    /** Đưa thông tin user vào session: ưu tiên DB rồi fallback Google profile */
    private void populateUserSessionFromPrincipal(OAuth2User principal, HttpSession session) {
        if (principal == null)
            return;

        String email = principal.getAttribute("email");
        String sub = principal.getAttribute("sub"); // google_id

        // 1) ƯU TIÊN lấy user từ DB theo email/sub
        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        if (user == null && sub != null) {
            user = userRepository.findByGoogleId(sub).orElse(null);
        }

        // 2) Chọn tên & ảnh
        String fullName = null;
        String picture = null;

        if (user != null) {
            fullName = user.getFullName(); // tên trong DB
            picture = user.getPicture(); // ảnh trong DB (nếu có)
        }

        // Fallback sang Google nếu DB chưa có
        if (fullName == null || fullName.isBlank()) {
            fullName = principal.getAttribute("name");
            if (fullName == null || fullName.isBlank()) {
                String given = principal.getAttribute("given_name");
                String family = principal.getAttribute("family_name");
                fullName = ((given != null ? given : "") + " " + (family != null ? family : "")).trim();
            }
        }
        if (picture == null || picture.isBlank()) {
            picture = principal.getAttribute("picture");
        }

        // 3) Set session để header hiển thị đúng
        session.setAttribute("full_name", fullName);
        session.setAttribute("picture", picture);
        session.setAttribute("email", email);
    }

    /** Thêm dữ liệu menu danh mục dùng chung */
    private void addCommonCategories(Model model) {
        model.addAttribute("categories", categoryMenuService.getMenuData());
    }

    /** Chọn view theo trạng thái đăng nhập */
    private String viewFor(OAuth2User principal) {
        return (principal != null) ? "homeUSER" : "homeGUEST";
    }

    /** ---------- Routes ---------- */

    // /login: luôn hợp nhất về /StudentMarket (1 link duy nhất)
    @GetMapping("/login")
    public String loginRedirect(@AuthenticationPrincipal OAuth2User principal) {
        return "redirect:/StudentMarket";
    }

    @GetMapping("/")
    public String rootToStudentMarket() {
        return "redirect:/StudentMarket";
    }

    // Trang chủ: nếu chưa đăng nhập → homeGUEST, đã đăng nhập → homeUSER
    @GetMapping({ "/StudentMarket" })
    public String homeUser(@AuthenticationPrincipal OAuth2User principal,
            HttpSession session,
            Model model) {

        populateUserSessionFromPrincipal(principal, session);

        // load danh sách sản phẩm
        List<Product> products = productRepository.findAllWithImages();
        model.addAttribute("products", products);
        addCommonCategories(model);

        // >>> ĐỌC FLASH (failureHandler/logoutSuccess set vào session)
        Object ferr = session.getAttribute("flash_error");
        if (ferr instanceof String && !((String) ferr).isBlank()) {
            model.addAttribute("error", (String) ferr);
            session.removeAttribute("flash_error");
        }
        Object flash = session.getAttribute("flash_success");
        if (flash instanceof String && !((String) flash).isBlank()) {
            model.addAttribute("success", (String) flash);
            session.removeAttribute("flash_success");
        }

        return viewFor(principal);
    }

    // Xem theo danh mục CHA (parent)
    @GetMapping("/StudentMarket/category/parent/{parentId}")
    public String byParent(@PathVariable Integer parentId,
            @AuthenticationPrincipal OAuth2User principal,
            HttpSession session,
            Model model) {

        populateUserSessionFromPrincipal(principal, session);

        addCommonCategories(model);
        model.addAttribute("products", productRepository.findByChildCategory_Parent_ParentId(parentId));
        model.addAttribute("activeParentId", parentId);
        return viewFor(principal);
    }

    // Xem theo danh mục CON (child)
    @GetMapping("/StudentMarket/category/child/{childId}")
    public String byChild(@PathVariable Integer childId,
            @AuthenticationPrincipal OAuth2User principal,
            HttpSession session,
            Model model) {

        populateUserSessionFromPrincipal(principal, session);

        addCommonCategories(model);
        model.addAttribute("products", productRepository.findByChildCategory_ChildId(childId));
        model.addAttribute("activeChildId", childId);
        return viewFor(principal);
    }
}

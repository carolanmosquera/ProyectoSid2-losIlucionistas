// src/main/java/co/icesi/UniPlan/controller/AdminController.java
package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Nullable
    private final AppUserService appUserService;

    public AdminController(@Autowired(required = false) AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/leader-requests")
    public String leaderRequests(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"));
        if (!isAdmin) return "redirect:/dashboard";

        if (appUserService != null) {
            model.addAttribute("requests", appUserService.findPendingLeaderRequests());
        }
        return "admin-leader-requests";
    }

    @PostMapping("/leader-requests/{id}/approve")
    public String approve(@PathVariable String id,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {
        if (appUserService == null) return "redirect:/dashboard";
        try {
            appUserService.approveLeaderRequest(id, auth.getName());
            redirectAttributes.addFlashAttribute("success", "Solicitud aprobada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leader-requests";
    }

    @PostMapping("/leader-requests/{id}/reject")
    public String reject(@PathVariable String id,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        if (appUserService == null) return "redirect:/dashboard";
        try {
            appUserService.rejectLeaderRequest(id, auth.getName());
            redirectAttributes.addFlashAttribute("success", "Solicitud rechazada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leader-requests";
    }
}
package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.dto.AppUserRegistrationRequest;
import co.icesi.UniPlan.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Nullable
    private final AppUserService appUserService;

    public LoginController(@Autowired(required = false) AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered,
            Model model) {
        if (error != null)
            model.addAttribute("loginError", true);
        if (logout != null)
            model.addAttribute("logoutMsg", true);
        if (registered != null)
            model.addAttribute("registerSuccess", true);
        return "login";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String institutionalId,
            @RequestParam String institutionalEmail,
            @RequestParam String passwordHash,
            @RequestParam(defaultValue = "STUDENT") String userType,
            @RequestParam(defaultValue = "false") boolean requestLeader, 
            Model model) {

        if (appUserService == null) {
            model.addAttribute("registerError", "Servicio no disponible.");
            return "login";
        }
        try {
            appUserService.registerAppUser(new AppUserRegistrationRequest(
                    institutionalId, institutionalEmail, passwordHash, null, userType),
                requestLeader);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("registerError", e.getMessage());
            return "login";
        }
    }
}
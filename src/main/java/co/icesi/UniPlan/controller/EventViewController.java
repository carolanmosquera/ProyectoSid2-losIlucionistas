package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.dto.EventEnrollmentRequest;
import co.icesi.UniPlan.model.User;
import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.repository.UserRepository;
import co.icesi.UniPlan.repository.mongo.AppUserRepository;
import co.icesi.UniPlan.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EventViewController {

    @Nullable
    private final EventService eventService;
    @Nullable
    private final AppUserRepository appUserRepository;
    private final UserRepository userRepository;

    public EventViewController(
            @Autowired(required = false) EventService eventService,
            @Autowired(required = false) AppUserRepository appUserRepository,
            UserRepository userRepository) {
        this.eventService = eventService;
        this.appUserRepository = appUserRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable String id, Authentication auth, Model model) {
        if (eventService == null) return "redirect:/home";
        try {
            Event event = eventService.findById(id);
            model.addAttribute("event", event);
            model.addAttribute("spotsLeft", event.getAvailableSlots() != null ? event.getAvailableSlots() : 0);

            try {
                model.addAttribute("stats", eventService.statistics(id));
            } catch (Exception e) {
                model.addAttribute("stats", null);
            }

            // 1. Obtener username de PostgreSQL (ej: "laura.h")
            String username = auth != null ? auth.getName() : null;
            AppUser currentUser = null;
            boolean isEnrolled = false;

            if (username != null && appUserRepository != null) {
                // 2. Buscar en PostgreSQL para obtener el student_id real (ej: "2001")
                User pgUser = userRepository.findByUsername(username).orElse(null);

                if (pgUser != null && pgUser.getStudent() != null) {
                    // 3. Buscar AppUser en MongoDB por el student_id de PostgreSQL
                    String studentId = pgUser.getStudent().getId();
                    currentUser = appUserRepository.findByInstitutionalId(studentId).orElse(null);
                } else if (pgUser != null && pgUser.getEmployee() != null) {
                    // Para empleados buscar por employee id
                    String employeeId = pgUser.getEmployee().getId();
                    currentUser = appUserRepository.findByInstitutionalId(employeeId).orElse(null);
                }

                // 4. Verificar si ya está inscrito
                if (currentUser != null && event.getInscriptions() != null) {
                    final String appUserId = currentUser.getId();
                    final String instId = currentUser.getInstitutionalId();
                    isEnrolled = event.getInscriptions().stream()
                            .anyMatch(i -> "active".equalsIgnoreCase(i.getStatus())
                                    && (appUserId.equals(i.getStudentId())
                                            || instId.equals(i.getInstitutionalId())));
                }
            }

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isEnrolled", isEnrolled);
            model.addAttribute("enrollSuccess", false);
            model.addAttribute("cancelSuccess", false);

        } catch (Exception e) {
            model.addAttribute("event", null);
        }
        return "event-detail";
    }

    @GetMapping("/events/{id}/confirmed")
    public String eventDetailConfirmed(
            @PathVariable String id,
            @RequestParam(required = false) String msg,
            @RequestParam(required = false) String err,
            Authentication auth, Model model) {
        eventDetail(id, auth, model);
        if (msg != null) model.addAttribute("enrollSuccess", true);
        if (err != null) model.addAttribute("enrollError", err);
        return "event-detail";
    }

    @PostMapping("/events/{id}/enroll")
    public String enroll(@PathVariable String id,
                         @RequestParam(required = false) String studentId,
                         @RequestParam(required = false) String institutionalId) {
        if (eventService == null) return "redirect:/home";
        try {
            eventService.enroll(id, new EventEnrollmentRequest(studentId, institutionalId));
            return "redirect:/events/" + id + "/confirmed?msg=ok";
        } catch (Exception e) {
            return "redirect:/events/" + id + "/confirmed?err=" + e.getMessage();
        }
    }

    @PostMapping("/events/{id}/cancel")
    public String cancel(@PathVariable String id,
                         @RequestParam(required = false) String studentId) {
        if (eventService == null) return "redirect:/home";
        try {
            eventService.cancelEnrollment(id, studentId);
            return "redirect:/events/" + id + "/confirmed?msg=cancelled";
        } catch (Exception e) {
            return "redirect:/events/" + id + "/confirmed?err=" + e.getMessage();
        }
    }
}
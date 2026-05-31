package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.service.AppUserService;
import co.icesi.UniPlan.service.EventService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Nullable
    private final EventService eventService;

    @Nullable
    private final AppUserService appUserService;

     public HomeController(
            @Autowired(required = false) EventService eventService,
            @Autowired(required = false) AppUserService appUserService) {  // ← agregar parámetro
        this.eventService = eventService;
        this.appUserService = appUserService;       // ← inicializar
    }

    @GetMapping("/home")
    public String home(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Authentication auth,
            Model model) {

        if (auth != null) {
            boolean isOrganizer = auth.getAuthorities().stream()
                    .anyMatch(a -> {
                        String r = a.getAuthority();
                        return r.contains("ORGANIZER") || r.contains("ADMIN")
                        || r.contains("PROFESSOR") || r.contains("INSTRUCTOR")
                        || r.contains("BU_STAFF");
                    });
            if (isOrganizer)
                return "redirect:/dashboard";
        }

        if (eventService != null) {
            try {
                Instant startInstant = (start != null && !start.isBlank()) ? Instant.parse(start) : null;
                Instant endInstant = (end != null && !end.isBlank()) ? Instant.parse(end) : null;
                List<Event> events = eventService.findAll(type, status, startInstant, endInstant);
                model.addAttribute("events", events);
            } catch (Exception e) {
                model.addAttribute("events", List.of());
            }
        } else {
            model.addAttribute("events", List.of());
        }

        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStart", start);
        model.addAttribute("selectedEnd", end);

        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";

        boolean isOrganizer = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String r = a.getAuthority();
                    return r.contains("ORGANIZER") || r.contains("ADMIN")
                        || r.contains("PROFESSOR") || r.contains("INSTRUCTOR")
                        || r.contains("BU_STAFF") || r.contains("STUDENT_LEADER");
                });

        if (!isOrganizer)
            return "redirect:/home";

        if (eventService != null) {
            try {
                List<Event> events = eventService.findAll(null, null, null, null);
                model.addAttribute("events", events);
                model.addAttribute("canModifyByEventId", events.stream()
                        .collect(Collectors.toMap(Event::getId, eventService::canModify)));
                model.addAttribute("activeInscriptionsByEventId", events.stream()
                        .collect(Collectors.toMap(Event::getId, eventService::activeInscriptionCount)));

                long published = events.stream()
                        .filter(e -> "published".equalsIgnoreCase(e.getStatus())).count();
                long upcoming = events.stream()
                        .filter(e -> e.getStartDate() != null
                                && e.getStartDate().isAfter(Instant.now()))
                        .count();
                long inscriptions = events.stream()
                        .mapToLong(eventService::activeInscriptionCount)
                        .sum();

                model.addAttribute("totalPublished", published);
                model.addAttribute("totalUpcoming", upcoming);
                model.addAttribute("totalInscriptions", inscriptions);
            } catch (Exception e) {
                model.addAttribute("events", List.of());
                model.addAttribute("canModifyByEventId", Map.of());
                model.addAttribute("activeInscriptionsByEventId", Map.of());
                model.addAttribute("totalPublished", 0);
                model.addAttribute("totalUpcoming", 0);
                model.addAttribute("totalInscriptions", 0);
            }
        } else {
            model.addAttribute("events", List.of());
            model.addAttribute("canModifyByEventId", Map.of());
            model.addAttribute("activeInscriptionsByEventId", Map.of());
            model.addAttribute("totalPublished", 0);
            model.addAttribute("totalUpcoming", 0);
            model.addAttribute("totalInscriptions", 0);
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"));
        if (isAdmin && appUserService != null) {
            model.addAttribute("pendingLeaderRequests",
                    appUserService.findPendingLeaderRequests().size());
        }

        return "dashboard";
    }
}

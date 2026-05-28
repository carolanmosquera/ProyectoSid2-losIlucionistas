package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.service.EventService;
import java.time.Instant;
import java.util.List;
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

    public HomeController(@Autowired(required = false) EventService eventService) {
        this.eventService = eventService;
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
                        return r.contains("ORGANIZER") || r.contains("ADMIN") || r.contains("STUDENT_LEADER");
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
                            || r.contains("PROFESSOR") || r.contains("WELFARE")
                            || r.contains("STUDENT_LEADER") || r.equals("ROLE_EMPLOYEE");
                });

        if (!isOrganizer)
            return "redirect:/home";

        if (eventService != null) {
            try {
                List<Event> events = eventService.findAll(null, null, null, null);
                model.addAttribute("events", events);

                long published = events.stream()
                        .filter(e -> "published".equalsIgnoreCase(e.getStatus())).count();
                long upcoming = events.stream()
                        .filter(e -> e.getStartDate() != null
                                && e.getStartDate().isAfter(Instant.now()))
                        .count();
                long inscriptions = events.stream()
                        .mapToLong(e -> e.getInscriptions() != null
                                ? e.getInscriptions().size()
                                : 0)
                        .sum();

                model.addAttribute("totalPublished", published);
                model.addAttribute("totalUpcoming", upcoming);
                model.addAttribute("totalInscriptions", inscriptions);
            } catch (Exception e) {
                model.addAttribute("events", List.of());
                model.addAttribute("totalPublished", 0);
                model.addAttribute("totalUpcoming", 0);
                model.addAttribute("totalInscriptions", 0);
            }
        } else {
            model.addAttribute("events", List.of());
            model.addAttribute("totalPublished", 0);
            model.addAttribute("totalUpcoming", 0);
            model.addAttribute("totalInscriptions", 0);
        }

        return "dashboard";
    }
}
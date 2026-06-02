error id: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/controller/EventViewController.java:_empty_/AppUser#getId#
file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/controller/EventViewController.java
empty definition using pc, found symbol in pc: _empty_/AppUser#getId#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 10693
uri: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/controller/EventViewController.java
text:
```scala
package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.dto.EventEnrollmentRequest;
import co.icesi.UniPlan.model.User;
import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.EventDetails;
import co.icesi.UniPlan.repository.ProgramRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
public class EventViewController {

    @Nullable
    private final EventService eventService;
    @Nullable
    private final AppUserRepository appUserRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;

    public EventViewController(
            @Autowired(required = false) EventService eventService,
            @Autowired(required = false) AppUserRepository appUserRepository,
            UserRepository userRepository,
            ProgramRepository programRepository){
        this.eventService = eventService;
        this.appUserRepository = appUserRepository;
        this.userRepository = userRepository;
        this.programRepository = programRepository;   
    }

    /**
     * Parsea fechas en cualquier formato que pueda llegar:
     * "2025-06-01T10:00" → datetime-local sin segundos (el más común)
     * "2025-06-01T10:00:00" → datetime-local con segundos
     * "2025-06-01T10:00:00Z" → ISO-8601 completo (si el JS logró convertirlo)
     */
    private Instant parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La fecha no puede estar vacía");
        }
        // 1) Ya tiene Z → es ISO completo, parsear directo
        if (value.endsWith("Z")) {
            return Instant.parse(value);
        }
        // 2) Tiene zona (+HH:mm) → OffsetDateTime
        if (value.contains("+") || (value.length() > 19 && value.charAt(19) == '-')) {
            return Instant.parse(value.length() == 16
                    ? value + ":00Z"
                    : value + "Z");
        }
        // 3) Sin zona — formato datetime-local: "yyyy-MM-ddTHH:mm" o
        // "yyyy-MM-ddTHH:mm:ss"
        // Se usa la zona de Colombia (UTC-5) para que la fecha no quede 5h atrás
        ZoneOffset colombiaOffset = ZoneOffset.of("-05:00");
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(colombiaOffset);
        } catch (DateTimeParseException e) {
            // "2025-06-01T10:00" (sin segundos)
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                    .toInstant(colombiaOffset);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EVENT DETAIL
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable String id, Authentication auth, Model model) {
        if (eventService == null)
            return "redirect:/home";
        try {
            Event event = eventService.findById(id);
            model.addAttribute("event", event);
            model.addAttribute("spotsLeft", event.getAvailableSlots() != null ? event.getAvailableSlots() : 0);

            try {
                model.addAttribute("stats", eventService.statistics(id));
            } catch (Exception e) {
                model.addAttribute("stats", null);
            }

            String email = auth != null ? auth.getName() : null;
            AppUser currentUser = null;
            boolean isEnrolled = false;

            if (email != null && appUserRepository != null) {

                currentUser = appUserRepository.findByInstitutionalEmail(email).orElse(null);

                if (currentUser != null && event.getInscriptions() != null) {
                    final String appUserId = currentUser.getInstitutionalId();
                    isEnrolled = event.getInscriptions().stream()
                            .anyMatch(i -> "active".equalsIgnoreCase(i.getStatus())
                                    && (appUserId.equals(i.getInstitutionalId())));
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
        if (msg != null)
            model.addAttribute("enrollSuccess", true);
        if (err != null)
            model.addAttribute("enrollError", err);
        return "event-detail";
    }

    @PostMapping("/events/{id}/enroll")
    public String enroll(
            @PathVariable String id,
            @RequestParam(required = false) String institutionalId,
            RedirectAttributes redirectAttributes) {

        if (eventService == null)
            return "redirect:/home";

        try {

            eventService.enroll(
                    id,
                    new EventEnrollmentRequest(institutionalId));

            redirectAttributes.addFlashAttribute(
                    "enrollSuccess",
                    true);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "enrollError",
                    e.getMessage());
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/cancel")
    public String cancel(
            @PathVariable String id,
            @RequestParam(required = false) String institutionalId,
            RedirectAttributes redirectAttributes) {

        if (eventService == null)
            return "redirect:/home";

        try {

            eventService.cancelEnrollment(id, institutionalId);

            redirectAttributes.addFlashAttribute(
                    "cancelSuccess",
                    true);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "enrollError",
                    e.getMessage());
        }

        return "redirect:/events/" + id;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE EVENT
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/events/create")
    public String createEventForm(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";
            model.addAttribute("programs", programRepository.findAll());
        return "create-event";
    }

    @PostMapping("/events/create")
    public String createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam String location,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam Integer maxSlots,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String tournamentType,
            @RequestParam(required = false) Integer teamsQuantity,
            @RequestParam(required = false) String totalHours,
            @RequestParam(required = false) Integer prerequisites, 
            Authentication auth,
            Model model) {

        if (eventService == null)
            return "redirect:/dashboard";

        try {
            String username = auth != null ? auth.getName() : null;
            AppUser organizer = null;
            if (username != null && appUserRepository != null) {
                organizer = appUserRepository.findByInstitutionalEmail(username).orElse(null);
                if (organizer == null) {
                    User pgUser = userRepository.findByUsername(username).orElse(null);
                    if (pgUser != null && pgUser.getEmployee() != null) {
                        organizer = appUserRepository
                                .findByInstitutionalId(pgUser.getEmployee().getId()).orElse(null);
                    }
                }
            }

            // Construir EventDetails con el código del programa como prerequisito
            EventDetails eventDetails = null;
            if (prerequisites != null) {
                eventDetails = EventDetails.builder()
                        .prerequisites(String.valueOf(prerequisites)) // guarda el código del programa
                        .build();
            }

            Event event = Event.builder()
                    .title(title)
                    .description(description)
                    .type(type)
                    .location(location)
                    .startDate(parseDate(startDate)) // ← usa el helper robusto
                    .endDate(parseDate(endDate)) // ← usa el helper robusto
                    .maxSlots(maxSlots)
                    .sportType(sportType)
                    .tournamentType(tournamentType)
                    .teamsQuantity(teamsQuantity)
                    .totalHours(totalHours)
                    .eventDetails(eventDetails) 
                    .organizerId(organizer != null ? organizer.g@@etId() : null)
                    .organizerType(organizer != null ? organizer.getUserType() : null)
                    .build();

            eventService.create(event);
            return "redirect:/dashboard?created";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("programs", programRepository.findAll());
            return "create-event";
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/AppUser#getId#
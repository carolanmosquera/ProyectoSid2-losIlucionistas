package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.dto.EventEnrollmentRequest;
import co.icesi.UniPlan.model.User;
import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.EventDetails;
import co.icesi.UniPlan.model.mongo.SpeakerInfo;
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
import java.util.Arrays;
import java.util.List;

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

    private String formatForDateTimeLocal(Instant value) {
        if (value == null) {
            return "";
        }
        return LocalDateTime.ofInstant(value, ZoneOffset.of("-05:00"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
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

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable String id, Authentication auth, Model model,
            RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/login";
        }
        if (eventService == null) {
            return "redirect:/dashboard";
        }
        try {
            Event event = eventService.findById(id);
            if (!eventService.canModify(event)) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede editar un evento con inscripciones activas");
                return "redirect:/dashboard";
            }
            addFormModelAttributes(model, event, true);
            return "create-event";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/events/{id}/edit")
    public String editEvent(
            @PathVariable String id,
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
            @RequestParam(required = false, name = "eventDetails.requiredMaterials") String requiredMaterials,
            @RequestParam(required = false, name = "eventDetails.prerequisites") String prerequisites,
            @RequestParam(required = false, name = "eventDetails.minSemester") Integer minSemester,
            @RequestParam(required = false, name = "eventDetails.speakerInfo.name") String speakerName,
            @RequestParam(required = false, name = "eventDetails.speakerInfo.affiliation") String speakerMembership,
            @RequestParam(required = false, name = "eventDetails.speakerInfo.profile") String speakerProfile,
            @RequestParam(required = false, name = "eventDetails.playerPerTeam") Integer playerPerTeam,
            @RequestParam(required = false, name = "eventDetails.helpedCommunity") String helpedCommunity,
            @RequestParam(required = false, name = "eventDetails.logisticInfo") String logisticInfo,
            @RequestParam(required = false, name = "eventDetails.reason") String reason,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (eventService == null) {
            return "redirect:/dashboard";
        }

        try {
            Event current = eventService.findById(id);
            Event event = Event.builder()
                    .title(title)
                    .description(description)
                    .type(type)
                    .location(location)
                    .startDate(parseDate(startDate))
                    .endDate(parseDate(endDate))
                    .maxSlots(maxSlots)
                    .sportType(sportType)
                    .tournamentType(tournamentType)
                    .teamsQuantity(teamsQuantity)
                    .totalHours(totalHours)
                    .organizationId(current.getOrganizationId())
                    .organizerId(current.getOrganizerId())
                    .organizerType(current.getOrganizerType())
                    .eventDetails(buildEventDetails(requiredMaterials, prerequisites, minSemester,
                            speakerName, speakerMembership, speakerProfile, playerPerTeam,
                            helpedCommunity, logisticInfo, reason))
                    .status(current.getStatus())
                    .build();

            eventService.update(id, event);
            redirectAttributes.addFlashAttribute("success", "Evento actualizado correctamente");
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Event event = Event.builder()
                    .id(id)
                    .title(title)
                    .description(description)
                    .type(type)
                    .location(location)
                    .maxSlots(maxSlots)
                    .sportType(sportType)
                    .tournamentType(tournamentType)
                    .teamsQuantity(teamsQuantity)
                    .totalHours(totalHours)
                    .eventDetails(buildEventDetails(requiredMaterials, prerequisites, minSemester,
                            speakerName, speakerMembership, speakerProfile, playerPerTeam,
                            helpedCommunity, logisticInfo, reason))
                    .build();
            addFormModelAttributes(model, event, true);
            model.addAttribute("startDateValue", startDate);
            model.addAttribute("endDateValue", endDate);
            return "create-event";
        }
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable String id, RedirectAttributes redirectAttributes) {
        if (eventService == null) {
            return "redirect:/dashboard";
        }
        try {
            eventService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Evento eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/events/create")
    public String createEventForm(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";
        addFormModelAttributes(model, null, false);
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
            @RequestParam(required = false, name = "eventDetails.requiredMaterials") String requiredMaterials,
            @RequestParam(required = false, name = "eventDetails.prerequisites") String prerequisites,
            @RequestParam(required = false, name = "eventDetails.minSemester") Integer minSemester,
            @RequestParam(required = false, name = "eventDetails.speakerInfo.name") String speakerName,
            @RequestParam(required = false, name = "eventDetails.speakerInfo.affiliation") String speakerMembership,
            @RequestParam(required = false, name = "eventDetails.speakerInfo.profile") String speakerProfile,
            @RequestParam(required = false, name = "eventDetails.playerPerTeam") Integer playerPerTeam,
            @RequestParam(required = false, name = "eventDetails.helpedCommunity") String helpedCommunity,
            @RequestParam(required = false, name = "eventDetails.logisticInfo") String logisticInfo,
            @RequestParam(required = false, name = "eventDetails.reason") String reason,
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
            if (prerequisites != null || minSemester != null) {
                eventDetails = EventDetails.builder()
                        .prerequisites(prerequisites != null
                                ? String.valueOf(prerequisites)
                                : null)
                        .minSemester(minSemester)               // ← agregar
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
                    .eventDetails(buildEventDetails(requiredMaterials, prerequisites, minSemester,
                            speakerName, speakerMembership, speakerProfile, playerPerTeam,
                            helpedCommunity, logisticInfo, reason))
                    .organizerId(organizer != null ? organizer.getId() : null)
                    .organizerType(organizer != null ? organizer.getUserType() : null)
                    .build();

            eventService.create(event);
            return "redirect:/dashboard?created";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            addFormModelAttributes(model, null, false);
            return "create-event";
        }
    }

    private void addFormModelAttributes(Model model, Event event, boolean editMode) {
        model.addAttribute("programs", programRepository.findAll());
        model.addAttribute("event", event);
        model.addAttribute("editMode", editMode);
        model.addAttribute("formAction", editMode && event != null
                ? "/events/" + event.getId() + "/edit"
                : "/events/create");
        model.addAttribute("startDateValue", event == null ? "" : formatForDateTimeLocal(event.getStartDate()));
        model.addAttribute("endDateValue", event == null ? "" : formatForDateTimeLocal(event.getEndDate()));
        model.addAttribute("requiredMaterialsValue", joinRequiredMaterials(event));
    }

    private EventDetails buildEventDetails(
            String requiredMaterials,
            String prerequisites,
            Integer minSemester,
            String speakerName,
            String speakerMembership,
            String speakerProfile,
            Integer playerPerTeam,
            String helpedCommunity,
            String logisticInfo,
            String reason) {

        SpeakerInfo speakerInfo = null;
        if (!isBlank(speakerName) || !isBlank(speakerMembership) || !isBlank(speakerProfile)) {
            speakerInfo = SpeakerInfo.builder()
                    .name(blankToNull(speakerName))
                    .membership(blankToNull(speakerMembership))
                    .profile(blankToNull(speakerProfile))
                    .build();
        }

        List<String> materials = parseRequiredMaterials(requiredMaterials);
        if (materials.isEmpty()
                && isBlank(prerequisites)
                && minSemester == null
                && speakerInfo == null
                && playerPerTeam == null
                && isBlank(helpedCommunity)
                && isBlank(logisticInfo)
                && isBlank(reason)) {
            return null;
        }

        return EventDetails.builder()
                .requiredMaterials(materials.isEmpty() ? null : materials)
                .prerequisites(blankToNull(prerequisites))
                .minSemester(minSemester)
                .speakerInfo(speakerInfo)
                .playerPerTeam(playerPerTeam)
                .helpedCommunity(blankToNull(helpedCommunity))
                .logisticInfo(blankToNull(logisticInfo))
                .reason(blankToNull(reason))
                .build();
    }

    private List<String> parseRequiredMaterials(String value) {
        if (isBlank(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("\\r?\\n|,"))
                .map(String::trim)
                .filter(material -> !material.isBlank())
                .toList();
    }

    private String joinRequiredMaterials(Event event) {
        if (event == null || event.getEventDetails() == null
                || event.getEventDetails().getRequiredMaterials() == null) {
            return "";
        }
        return String.join("\n", event.getEventDetails().getRequiredMaterials());
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

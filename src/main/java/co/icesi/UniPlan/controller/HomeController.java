package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.service.EventService;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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

    /**
     * Catálogo de eventos con filtros opcionales.
     * Ruta: GET /home?type=&status=&start=&end=
     *
     * El EventService.findAll() filtra por type, status, start, end.
     * Si no hay filtros, devuelve todos los eventos.
     *
     * NOTA: los eventos en MongoDB pueden tener status "published" —
     * el servicio debe manejar ese valor también, o actualiza los datos
     * de prueba a "UPCOMING".
     */
    @GetMapping("/home")
    public String home(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            Model model) {

        if (eventService != null) {
            try {
                Instant startInstant = (start != null && !start.isBlank()) ? Instant.parse(start) : null;
                Instant endInstant   = (end   != null && !end.isBlank())   ? Instant.parse(end)   : null;

                // Pasa null en status para traer TODOS los eventos sin importar el status
                List<Event> events = eventService.findAll(type, status, startInstant, endInstant);
                model.addAttribute("events", events);
            } catch (Exception e) {
                model.addAttribute("events", List.of());
                model.addAttribute("error", "Error cargando eventos: " + e.getMessage());
            }
        } else {
            model.addAttribute("events", List.of());
        }

        // Parámetros actuales para mantener filtros en el formulario
        model.addAttribute("selectedType",   type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStart",  start);
        model.addAttribute("selectedEnd",    end);

        return "home";
    }
}
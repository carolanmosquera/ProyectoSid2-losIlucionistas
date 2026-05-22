package co.icesi.UniPlan.service;

import co.icesi.UniPlan.dto.EventStatisticsResponse;
import co.icesi.UniPlan.dto.EventTypeReportResponse;
import co.icesi.UniPlan.dto.StudentParticipationReportResponse;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.Inscription;
import co.icesi.UniPlan.repository.mongo.EventRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(MongoTemplate.class)
public class ReportService {

    private final EventRepository eventRepository;
    private final EventService eventService;

    public ReportService(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    public List<EventTypeReportResponse> eventTypeReport() {
        Map<String, List<Event>> eventsByType = eventRepository.findAll().stream()
                .collect(Collectors.groupingBy(event -> event.getType() == null ? "sin_tipo" : event.getType()));

        return eventsByType.entrySet().stream()
                .map(entry -> {
                    long enrollments = entry.getValue().stream().flatMap(event -> safeInscriptions(event).stream())
                            .filter(this::isActive)
                            .count();
                    double averageOccupancy = entry.getValue().stream()
                            .map(event -> eventService.statistics(event.getId()))
                            .mapToDouble(EventStatisticsResponse::occupancyPercentage)
                            .average()
                            .orElse(0);
                    return new EventTypeReportResponse(entry.getKey(), entry.getValue().size(), enrollments, averageOccupancy);
                })
                .toList();
    }

    public StudentParticipationReportResponse studentParticipation(String institutionalId) {
        long active = 0;
        long cancelled = 0;
        long attended = 0;

        for (Event event : eventRepository.findAll()) {
            for (Inscription inscription : safeInscriptions(event)) {
                if (!institutionalId.equals(inscription.getInstitutionalId())) {
                    continue;
                }
                if (isActive(inscription)) {
                    active++;
                }
                if ("cancelled".equalsIgnoreCase(inscription.getStatus())) {
                    cancelled++;
                }
                if (Boolean.TRUE.equals(inscription.getAttended())) {
                    attended++;
                }
            }
        }

        return new StudentParticipationReportResponse(institutionalId, institutionalId, active, cancelled, attended);
    }

    private List<Inscription> safeInscriptions(Event event) {
        return event.getInscriptions() == null ? List.of() : event.getInscriptions();
    }

    private boolean isActive(Inscription inscription) {
        return "active".equalsIgnoreCase(inscription.getStatus());
    }
}

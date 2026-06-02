package co.icesi.UniPlan.service;

import co.icesi.UniPlan.dto.EventStatisticsDetailResponse;
import co.icesi.UniPlan.dto.RegisterExportRequest;
import co.icesi.UniPlan.exception.ResourceNotFoundException;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.EventStatistics;
import co.icesi.UniPlan.model.mongo.Inscription;
import co.icesi.UniPlan.repository.mongo.EventRepository;
import co.icesi.UniPlan.repository.mongo.EventStatisticsRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventStatisticsService {

    private final EventStatisticsRepository statisticsRepository;
    private final EventRepository eventRepository;

    public EventStatisticsService(
            EventStatisticsRepository statisticsRepository,
            EventRepository eventRepository) {
        this.statisticsRepository = statisticsRepository;
        this.eventRepository = eventRepository;
    }

    /** Obtiene o crea el documento de estadísticas para un evento. */
    public EventStatisticsDetailResponse getByEventId(String eventId) {
        Event event = findEvent(eventId);
        EventStatistics stats = statisticsRepository.findByEventId(eventId)
                .orElseGet(() -> createInitial(event));
        return toResponse(stats, event);
    }

    /**
     * Registra una inscripción nueva en el historial diario
     * y actualiza la fecha pico si corresponde.
     * Llamar desde EventService.enroll().
     */
    public void recordInscription(String eventId) {
        Event event = findEvent(eventId);
        EventStatistics stats = statisticsRepository.findByEventId(eventId)
                .orElseGet(() -> createInitial(event));

        String today = LocalDate.now().toString();
        DailyRecordHelper.incrementInscriptions(stats.getDailyInscriptions(), today);

        // Actualiza fecha pico si el número de activas supera el máximo previo
        long currentActive = countActive(event);
        if (stats.getPeakInscriptionsDate() == null || currentActive >= peakCount(stats)) {
            stats.setPeakInscriptionsDate(Instant.now());
        }

        stats.setLastUpdated(Instant.now());
        statisticsRepository.save(stats);
    }

    /**
     * Registra una cancelación en el historial diario.
     * Llamar desde EventService.cancelEnrollment().
     */
    public void recordCancellation(String eventId) {
        Event event = findEvent(eventId);
        EventStatistics stats = statisticsRepository.findByEventId(eventId)
                .orElseGet(() -> createInitial(event));

        String today = LocalDate.now().toString();
        DailyRecordHelper.incrementCancellations(stats.getDailyInscriptions(), today);

        stats.setLastUpdated(Instant.now());
        statisticsRepository.save(stats);
    }

    /** Registra que un usuario exportó un reporte del evento. */
    public EventStatisticsDetailResponse registerExport(String eventId, RegisterExportRequest request) {
        Event event = findEvent(eventId);
        EventStatistics stats = statisticsRepository.findByEventId(eventId)
                .orElseGet(() -> createInitial(event));

        EventStatistics.ReportExport export = EventStatistics.ReportExport.builder()
                .exportedBy(request.exportedBy())
                .exportedAt(Instant.now())
                .format(request.format().toUpperCase())
                .build();

        stats.getReportExports().add(export);
        stats.setLastUpdated(Instant.now());
        statisticsRepository.save(stats);
        return toResponse(stats, event);
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private EventStatistics createInitial(Event event) {
        EventStatistics stats = EventStatistics.builder()
                .eventId(event.getId())
                .eventCode(event.getEventCode())
                .dailyInscriptions(new ArrayList<>())
                .reportExports(new ArrayList<>())
                .lastUpdated(Instant.now())
                .build();
        return statisticsRepository.save(stats);
    }

    private EventStatisticsDetailResponse toResponse(EventStatistics stats, Event event) {
        List<Inscription> inscriptions = event.getInscriptions() == null
                ? List.of() : event.getInscriptions();

        long active = inscriptions.stream()
                .filter(i -> "active".equalsIgnoreCase(i.getStatus())).count();
        long cancelled = inscriptions.stream()
                .filter(i -> "cancelled".equalsIgnoreCase(i.getStatus())).count();
        long attended = inscriptions.stream()
                .filter(i -> Boolean.TRUE.equals(i.getAttended())).count();
        long total = active + cancelled;
        double occupancy = (event.getMaxSlots() == null || event.getMaxSlots() <= 0)
                ? 0 : active * 100.0 / event.getMaxSlots();
        double cancellationRate = total == 0 ? 0 : cancelled * 100.0 / total;

        List<EventStatisticsDetailResponse.DailyRecord> dailyRecords = stats.getDailyInscriptions()
                .stream()
                .map(d -> new EventStatisticsDetailResponse.DailyRecord(
                        d.getDate(), d.getNewInscriptions(), d.getNewCancellations()))
                .toList();

        List<EventStatisticsDetailResponse.ReportExport> exports = stats.getReportExports()
                .stream()
                .map(e -> new EventStatisticsDetailResponse.ReportExport(
                        e.getExportedBy(), e.getExportedAt(), e.getFormat()))
                .toList();

        return new EventStatisticsDetailResponse(
                stats.getId(), stats.getEventId(), stats.getEventCode(),
                active, cancelled, attended, occupancy, cancellationRate,
                stats.getPeakInscriptionsDate(),
                dailyRecords, exports,
                stats.getLastUpdated());
    }

    private Event findEvent(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventId));
    }

    private long countActive(Event event) {
        if (event.getInscriptions() == null) return 0;
        return event.getInscriptions().stream()
                .filter(i -> "active".equalsIgnoreCase(i.getStatus())).count();
    }

    /** Aproxima el conteo pico buscando el día con más inscripciones acumuladas. */
    private long peakCount(EventStatistics stats) {
        return stats.getDailyInscriptions().stream()
                .mapToLong(EventStatistics.DailyRecord::getNewInscriptions)
                .sum();
    }

    // ── Helper interno para manejo del array diario ───────────────────────────
    private static class DailyRecordHelper {

        static void incrementInscriptions(List<EventStatistics.DailyRecord> records, String date) {
            records.stream()
                    .filter(r -> date.equals(r.getDate()))
                    .findFirst()
                    .ifPresentOrElse(
                            r -> r.setNewInscriptions(r.getNewInscriptions() + 1),
                            () -> records.add(EventStatistics.DailyRecord.builder()
                                    .date(date).newInscriptions(1).newCancellations(0).build()));
        }

        static void incrementCancellations(List<EventStatistics.DailyRecord> records, String date) {
            records.stream()
                    .filter(r -> date.equals(r.getDate()))
                    .findFirst()
                    .ifPresentOrElse(
                            r -> r.setNewCancellations(r.getNewCancellations() + 1),
                            () -> records.add(EventStatistics.DailyRecord.builder()
                                    .date(date).newInscriptions(0).newCancellations(1).build()));
        }
    }
}
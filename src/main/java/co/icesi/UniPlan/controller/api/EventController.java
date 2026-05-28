package co.icesi.UniPlan.controller.api;

import co.icesi.UniPlan.dto.EnrolledUserResponse;
import co.icesi.UniPlan.dto.EventEnrollmentRequest;
import co.icesi.UniPlan.dto.EventStatisticsResponse;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.Inscription;
import co.icesi.UniPlan.service.EventService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@ConditionalOnBean(MongoTemplate.class)
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<Event> findAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return eventService.findAll(type, status, start, end);
    }

    @GetMapping("/{id}")
    public Event findById(@PathVariable String id) {
        return eventService.findById(id);
    }

    @GetMapping("/code/{eventCode}")
    public Event findByCode(@PathVariable String eventCode) {
        return eventService.findByCode(eventCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@Valid @RequestBody Event event) {
        return eventService.create(event);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable String id, @Valid @RequestBody Event event) {
        return eventService.update(id, event);
    }

    @PostMapping("/{id}/inscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public Inscription enroll(@PathVariable String id, @Valid @RequestBody EventEnrollmentRequest request) {
        return eventService.enroll(id, request);
    }

    @PostMapping("/{id}/inscriptions/{studentId}/cancel")
    public Inscription cancelEnrollment(@PathVariable String id, @PathVariable String studentId) {
        return eventService.cancelEnrollment(id, studentId);
    }

    @GetMapping("/{id}/inscriptions")
    public List<EnrolledUserResponse> enrolledStudents(@PathVariable String id) {
        return eventService.enrolledStudents(id);
    }

    @GetMapping("/{id}/inscriptions.csv")
    public ResponseEntity<byte[]> enrolledStudentsCsv(@PathVariable String id) {
        byte[] csv = eventService.enrolledStudentsCsv(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("event-" + id + "-inscriptions.csv").build().toString())
                .body(csv);
    }

    @GetMapping("/{id}/statistics")
    public EventStatisticsResponse statistics(@PathVariable String id) {
        return eventService.statistics(id);
    }
}

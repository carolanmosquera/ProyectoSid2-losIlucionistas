error id: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/EventService.java
file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/EventService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[44,59]

error in qdox parser
file content:
```java
offset: 1831
uri: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/EventService.java
text:
```scala
package co.icesi.UniPlan.service;

import co.icesi.UniPlan.dto.EnrolledUserResponse;
import co.icesi.UniPlan.dto.EventEnrollmentRequest;
import co.icesi.UniPlan.dto.EventStatisticsResponse;
import co.icesi.UniPlan.exception.BusinessException;
import co.icesi.UniPlan.exception.ResourceNotFoundException;
import co.icesi.UniPlan.model.Enrollment;
import co.icesi.UniPlan.model.Student;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.EventDetails;
import co.icesi.UniPlan.model.mongo.Inscription;
import co.icesi.UniPlan.repository.EnrollmentRepository;
import co.icesi.UniPlan.repository.GroupRepository;
import co.icesi.UniPlan.repository.ProgramRepository;
import co.icesi.UniPlan.repository.StudentRepository;
import co.icesi.UniPlan.repository.mongo.AppUserRepository;
import co.icesi.UniPlan.repository.mongo.EventRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private static final String STATUS_PUBLISHED = "published";
    private static final String INSCRIPTION_ACTIVE = "active";
    private static final String INSCRIPTION_CANCELLED = "cancelled";

    private final EventRepository eventRepository;
    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EventStatisticsService eventStatisticsService;
    private final GroupRepository groupRepository;  
    private final ProgramRepository programRepository;

    public EventService(@@
            EventRepository eventRepository,
            AppUserRepository appUserRepository,
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            EventStatisticsService eventStatisticsService,) {
        this.eventRepository = eventRepository;
        this.appUserRepository = appUserRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventStatisticsService = eventStatisticsService;
    }

    public List<Event> findAll(String type, String status, Instant start, Instant end) {
        return eventRepository.findAll().stream()
                .filter(event -> isBlank(type) || equalsIgnoreCase(event.getType(), type))
                .filter(event -> isBlank(status) || equalsIgnoreCase(resolveTemporalStatus(event), status)
 equalsIgnoreCase(event.getStatus(), status))
                .filter(event -> start == null || !event.getStartDate().isBefore(start))
                .filter(event -> end == null || !event.getStartDate().isAfter(end))
                .sorted(Comparator.comparing(Event::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public Event findById(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
    }

    public Event findByCode(String eventCode) {
        return eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventCode));
    }

    public Event create(Event event) {
        validateEventForPublication(event);

        Instant now = Instant.now();
        event.setId(null);
        event.setEventCode(generateEventCode(event.getType()));
        event.setAvailableSlots(event.getMaxSlots());
        event.setStatus(defaultIfBlank(event.getStatus(), STATUS_PUBLISHED));
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event.setInscriptions(new ArrayList<>());
        return eventRepository.save(event);
    }

    public Event update(String id, Event event) {
        Event current = findById(id);
        validateEventForPublication(event);

        current.setTitle(event.getTitle());
        current.setDescription(event.getDescription());
        current.setType(event.getType());
        current.setStartDate(event.getStartDate());
        current.setEndDate(event.getEndDate());
        current.setLocation(event.getLocation());
        current.setMaxSlots(event.getMaxSlots());
        current.setOrganizationId(event.getOrganizationId());
        current.setOrganizerId(event.getOrganizerId());
        current.setOrganizerType(event.getOrganizerType());
        current.setEventDetails(event.getEventDetails());
        current.setSportType(event.getSportType());
        current.setTeamsQuantity(event.getTeamsQuantity());
        current.setTotalHours(event.getTotalHours());
        current.setTournamentFormat(event.getTournamentFormat());
        current.setTournamentType(event.getTournamentType());
        current.setStatus(defaultIfBlank(event.getStatus(), current.getStatus()));
        current.setAvailableSlots(calculateAvailableSlots(current));
        current.setUpdatedAt(Instant.now());
        return eventRepository.save(current);
    }

    public Inscription enroll(String eventId, EventEnrollmentRequest request) {
        Event event = findById(eventId);
        validateCanEnroll(event, request);

        Inscription inscription = Inscription.builder()
                .institutionalId(request.institutionalId())
                .status(INSCRIPTION_ACTIVE)
                .attended(false)
                .enrolledAt(Instant.now())
                .build();

        List<Inscription> inscriptions = mutableInscriptions(event);
        inscriptions.add(inscription);
        event.setInscriptions(inscriptions);
        event.setAvailableSlots(Math.max(0, nullToZero(event.getAvailableSlots()) - 1));
        event.setUpdatedAt(Instant.now());
        eventRepository.save(event);
        eventStatisticsService.recordInscription(eventId);
        return inscription;
    }

    public Inscription cancelEnrollment(String eventId, String institutionalId) {
        Event event = findById(eventId);
        Inscription inscription = activeInscriptionFor(event, institutionalId);
        inscription.setStatus(INSCRIPTION_CANCELLED);
        inscription.setCancelledAt(Instant.now());
        inscription.setAttended(false);
        event.setAvailableSlots(calculateAvailableSlots(event));
        event.setUpdatedAt(Instant.now());
        eventRepository.save(event);
        eventStatisticsService.recordCancellation(eventId);
        return inscription;
    }

    public List<EnrolledUserResponse> enrolledStudents(String eventId) {
        Event event = findById(eventId);
        return safeInscriptions(event).stream()
                .map(this::toEnrolledStudent)
                .toList();
    }

    public byte[] enrolledStudentsCsv(String eventId) {
        StringBuilder csv = new StringBuilder(
                "student_id,institutional_id,first_name,last_name,email,status,attended,enrolled_at,cancelled_at\n");
        enrolledStudents(eventId).forEach(student -> csv.append(csv(student.institutionalId())).append(',')
                .append(csv(student.firstName())).append(',')
                .append(csv(student.lastName())).append(',')
                .append(csv(student.email())).append(',')
                .append(csv(student.status())).append(',')
                .append(csv(String.valueOf(student.attended()))).append(',')
                .append(csv(String.valueOf(student.enrolledAt()))).append(',')
                .append(csv(String.valueOf(student.cancelledAt()))).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public EventStatisticsResponse statistics(String eventId) {
        Event event = findById(eventId);
        long enrolled = safeInscriptions(event).stream().filter(this::isActive).count();
        long cancellations = safeInscriptions(event).stream()
                .filter(inscription -> equalsIgnoreCase(inscription.getStatus(), INSCRIPTION_CANCELLED))
                .count();
        long attendees = safeInscriptions(event).stream()
                .filter(inscription -> Boolean.TRUE.equals(inscription.getAttended())).count();
        double occupancy = event.getMaxSlots() == null || event.getMaxSlots() <= 0
                ? 0
                : enrolled * 100.0 / event.getMaxSlots();
        return new EventStatisticsResponse(event.getId(), event.getEventCode(), enrolled, cancellations, attendees,
                occupancy);
    }

    private void validateEventForPublication(Event event) {
        if (isBlank(event.getTitle())) {
            throw new BusinessException("El evento debe tener titulo");
        }
        if (isBlank(event.getType())) {
            throw new BusinessException("El evento debe tener tipo");
        }
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new BusinessException("El evento debe tener fecha de inicio y fin");
        }
        if (event.getStartDate().isBefore(Instant.now())) {
            throw new BusinessException("No se puede publicar un evento con fecha pasada");
        }
        if (!event.getEndDate().isAfter(event.getStartDate())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        if (event.getMaxSlots() == null || event.getMaxSlots() <= 0) {
            throw new BusinessException("Los cupos maximos deben ser mayores a cero");
        }
    }

    private void validateCanEnroll(Event event, EventEnrollmentRequest request) {
        if (!STATUS_PUBLISHED.equalsIgnoreCase(event.getStatus())) {
            throw new BusinessException("El evento no esta publicado");
        }
        if (event.getStartDate().isBefore(Instant.now())) {
            throw new BusinessException("No se puede inscribir a un evento iniciado o finalizado");
        }
        if (nullToZero(event.getAvailableSlots()) <= 0) {
            throw new BusinessException("No hay cupos disponibles");
        }
        if (safeInscriptions(event).stream()
                .anyMatch(inscription -> isActive(inscription)
                        && sameStudent(inscription, request.institutionalId()))) {
            throw new BusinessException("El estudiante ya esta inscrito en este evento");
        }

        appUserRepository.findByInstitutionalId(request.institutionalId())
                .orElseThrow(() -> new BusinessException("El estudiante no esta registrado en UniPlan"));

        validateEventTypeRules(event, request);
    }

    private void validateEventTypeRules(Event event, EventEnrollmentRequest request) {
        String type = normalize(event.getType());
        if (type.contains("taller") || type.contains("workshop")) {
            validateWorkshopRule(event.getEventDetails(), request.institutionalId());
        }
        if (type.contains("torneo") || type.contains("sport") || type.contains("deport")) {
            validateTournamentOverlap(event, request.institutionalId());
        }
        if (type.contains("volunt")) {
            validateVolunteerRule(event);
        }
    }

    private void validateWorkshopRule(EventDetails details, String institutionalId) {
        if (details == null || details.getMinSemester() == null) {
            return;
        }
        int approvedEnrollments = enrollmentRepository.findByIdStudentId(institutionalId).stream()
                .map(Enrollment::getStatus)
                .filter(status -> status == null || !"cancelled".equalsIgnoreCase(status))
                .toList()
                .size();
        if (approvedEnrollments < details.getMinSemester()) {
            throw new BusinessException("El estudiante no cumple el semestre minimo requerido para el taller");
        }
    }

    private void validateTournamentOverlap(Event event, String institutionalId) {
        boolean overlaps = eventRepository.findAll().stream()
                .filter(other -> !Objects.equals(other.getId(), event.getId()))
                .filter(other -> normalize(other.getType()).contains("torneo")
 normalize(other.getType()).contains("sport")
 normalize(other.getType()).contains("deport"))
                .filter(other -> safeInscriptions(other).stream()
                        .anyMatch(inscription -> isActive(inscription)
                                && sameStudent(inscription, institutionalId)))
                .anyMatch(other -> event.getStartDate().isBefore(other.getEndDate())
                        && event.getEndDate().isAfter(other.getStartDate()));
        if (overlaps) {
            throw new BusinessException("El estudiante ya tiene un torneo traslapado en ese horario");
        }
    }

    private void validateVolunteerRule(Event event) {
        Integer requiredHours = parsePositiveInteger(event.getTotalHours());
        if (requiredHours != null && requiredHours <= 0) {
            throw new BusinessException("Las horas requeridas de voluntariado deben ser mayores a cero");
        }
    }

    private EnrolledUserResponse toEnrolledStudent(Inscription inscription) {
        Student student = studentRepository.findById(inscription.getInstitutionalId()).orElse(null);
        return new EnrolledUserResponse(
                inscription.getInstitutionalId(),
                student == null ? null : student.getFirstName(),
                student == null ? null : student.getLastName(),
                student == null ? null : student.getEmail(),
                inscription.getStatus(),
                inscription.getAttended(),
                inscription.getEnrolledAt(),
                inscription.getCancelledAt());
    }

    private Inscription activeInscriptionFor(Event event, String institutionalId) {
        return safeInscriptions(event).stream()
                .filter(inscription -> isActive(inscription) && sameStudent(inscription, institutionalId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inscripcion activa no encontrada para el estudiante: " + institutionalId));
    }

    private int calculateAvailableSlots(Event event) {
        long active = safeInscriptions(event).stream().filter(this::isActive).count();
        return Math.max(0, nullToZero(event.getMaxSlots()) - (int) active);
    }

    private String generateEventCode(String type) {
        String prefix = normalize(type).replaceAll("[^a-z0-9]", "").toUpperCase(Locale.ROOT);
        if (prefix.length() > 3) {
            prefix = prefix.substring(0, 3);
        }
        if (prefix.isBlank()) {
            prefix = "EVT";
        }
        String code;
        do {
            code = "EVT-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (eventRepository.existsByEventCode(code));
        return code;
    }

    private String resolveTemporalStatus(Event event) {
        Instant now = Instant.now();
        if (event.getStartDate() != null && event.getStartDate().isAfter(now)) {
            return "upcoming";
        }
        if (event.getStartDate() != null && event.getEndDate() != null
                && !event.getStartDate().isAfter(now) && !event.getEndDate().isBefore(now)) {
            return "in_progress";
        }
        return "finished";
    }

    private List<Inscription> mutableInscriptions(Event event) {
        return new ArrayList<>(safeInscriptions(event));
    }

    private List<Inscription> safeInscriptions(Event event) {
        return event.getInscriptions() == null ? List.of() : event.getInscriptions();
    }

    private boolean isActive(Inscription inscription) {
        return equalsIgnoreCase(inscription.getStatus(), INSCRIPTION_ACTIVE);
    }

    private boolean sameStudent(Inscription inscription, String institutionalId) {
        return Objects.equals(inscription.getInstitutionalId(), institutionalId);
    }

    private Integer parsePositiveInteger(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String csv(String value) {
        if (value == null || "null".equals(value)) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/EventService.java
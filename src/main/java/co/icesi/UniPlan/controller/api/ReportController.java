package co.icesi.UniPlan.controller.api;

import co.icesi.UniPlan.dto.EventTypeReportResponse;
import co.icesi.UniPlan.dto.StudentParticipationReportResponse;
import co.icesi.UniPlan.service.ReportService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@ConditionalOnBean(MongoTemplate.class)
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/events/by-type")
    public List<EventTypeReportResponse> eventTypeReport() {
        return reportService.eventTypeReport();
    }

    @GetMapping("/students/{institutionalId}/participation")
    public StudentParticipationReportResponse studentParticipation(@PathVariable String institutionalId) {
        return reportService.studentParticipation(institutionalId);
    }
}

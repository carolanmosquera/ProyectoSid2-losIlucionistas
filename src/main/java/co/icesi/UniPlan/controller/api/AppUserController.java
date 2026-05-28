package co.icesi.UniPlan.controller.api;

import co.icesi.UniPlan.dto.AppUserRegistrationRequest;
import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.service.AppUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appusers")
@ConditionalOnBean(MongoTemplate.class)
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUser> findAll() {
        return appUserService.findAll();
    }

    @GetMapping("/{id}")
    public AppUser findById(@PathVariable String id) {
        return appUserService.findById(id);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public AppUser registerStudent(@Valid @RequestBody AppUserRegistrationRequest request) {
        return appUserService.registerAppUser(request);
    }

    @PostMapping("/organizers")
    @ResponseStatus(HttpStatus.CREATED)
    public AppUser registerOrganizer(@Valid @RequestBody AppUserRegistrationRequest request) {
        return appUserService.registerOrganizer(request);
    }

    @PatchMapping("/{id}/status")
    public AppUser updateStatus(@PathVariable String id, @RequestParam String status) {
        return appUserService.updateStatus(id, status);
    }
}

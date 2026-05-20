package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.model.mongo.Event;
import co.icesi.UniPlan.model.mongo.Organization;
import co.icesi.UniPlan.model.mongo.Role;
import co.icesi.UniPlan.repository.mongo.AppUserRepository;
import co.icesi.UniPlan.repository.mongo.EventRepository;
import co.icesi.UniPlan.repository.mongo.OrganizationRepository;
import co.icesi.UniPlan.repository.mongo.RoleRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnBean(MongoTemplate.class)
@RestController
@RequestMapping("/api/mongo")
public class MongoController {

    private final AppUserRepository appUserRepository;
    private final EventRepository eventRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    public MongoController(
            AppUserRepository appUserRepository,
            EventRepository eventRepository,
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository) {
        this.appUserRepository = appUserRepository;
        this.eventRepository = eventRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/appusers")
    public List<AppUser> getAllAppUsers() {
        return appUserRepository.findAll();
    }

    @GetMapping("/appusers/{id}")
    public AppUser getAppUserById(@PathVariable String id) {
        return appUserRepository.findById(id).orElse(null);
    }

    @PostMapping("/appusers")
    @ResponseStatus(HttpStatus.CREATED)
    public AppUser createAppUser(@RequestBody AppUser appUser) {
        if (appUser.getCreatedAt() == null) {
            appUser.setCreatedAt(Instant.now());
        }
        return appUserRepository.save(appUser);
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/events/{id}")
    public Event getEventById(@PathVariable String id) {
        return eventRepository.findById(id).orElse(null);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Event createEvent(@RequestBody Event event) {
        Instant now = Instant.now();
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);
        return eventRepository.save(event);
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public Role createRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }

    @GetMapping("/organizations")
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @PostMapping("/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    public Organization createOrganization(@RequestBody Organization organization) {
        return organizationRepository.save(organization);
    }
}

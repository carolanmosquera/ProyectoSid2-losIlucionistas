package co.icesi.UniPlan.config;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.model.mongo.Permission;
import co.icesi.UniPlan.model.mongo.Role;
import co.icesi.UniPlan.repository.mongo.AppUserRepository;
import co.icesi.UniPlan.repository.mongo.PermissionRepository;
import co.icesi.UniPlan.repository.mongo.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (permissionRepository.count() == 0) {
            createPermissions();
        }

        if (roleRepository.count() == 0) {
            createRoles();
        }

        // Siempre intentar crear usuarios — createUser() verifica por email
        createUsers();
    }

    private void createPermissions() {
        savePermission("USER_LIST", "Listar usuarios");
        savePermission("USER_CREATE", "Crear usuarios");
        savePermission("USER_EDIT", "Editar usuarios");
        savePermission("USER_DELETE", "Eliminar usuarios");
        savePermission("ROLE_LIST", "Listar roles");
        savePermission("ROLE_CREATE", "Crear roles");
        savePermission("ROLE_EDIT", "Editar roles");
        savePermission("ROLE_DELETE", "Eliminar roles");
        savePermission("PERMISSION_LIST", "Listar permisos");
        savePermission("EVENT_READ", "Consultar eventos");
        savePermission("EVENT_CREATE", "Crear eventos");
        savePermission("EVENT_UPDATE", "Actualizar eventos");
        savePermission("EVENT_DELETE", "Eliminar eventos");
        savePermission("EVENT_PUBLISH", "Publicar eventos");
        savePermission("ENROLLMENT_READ", "Consultar inscripciones");
        savePermission("ENROLLMENT_CREATE", "Inscribirse a eventos");
        savePermission("ENROLLMENT_CANCEL", "Cancelar inscripción");
        savePermission("ATTENDEE_LIST", "Listar inscritos de un evento");
        savePermission("ATTENDEE_EXPORT", "Exportar lista de inscritos en CSV");
        savePermission("STATS_READ", "Consultar estadísticas de eventos");
    }

    private void savePermission(String resource, String action) {
        permissionRepository.save(Permission.builder()
                .resource(resource)
                .actions(List.of(action))
                .build());
    }

    private void createRoles() {
        createRole("ADMIN", "Administrador general del sistema",
                List.of("USER_LIST", "USER_CREATE", "USER_EDIT", "USER_DELETE",
                        "ROLE_LIST", "ROLE_CREATE", "ROLE_EDIT", "ROLE_DELETE",
                        "PERMISSION_LIST",
                        "EVENT_READ", "EVENT_CREATE", "EVENT_UPDATE", "EVENT_DELETE", "EVENT_PUBLISH",
                        "ENROLLMENT_READ", "ENROLLMENT_CREATE", "ENROLLMENT_CANCEL",
                        "ATTENDEE_LIST", "ATTENDEE_EXPORT", "STATS_READ"));

        createRole("ORGANIZER_PROFESSOR", "Profesor organizador de eventos",
                List.of("EVENT_READ", "EVENT_CREATE", "EVENT_UPDATE", "EVENT_DELETE", "EVENT_PUBLISH",
                        "ATTENDEE_LIST", "ATTENDEE_EXPORT"));

        createRole("ORGANIZER_STUDENT_LEADER", "Líder estudiantil organizador de eventos",
                List.of("EVENT_READ", "EVENT_CREATE", "EVENT_UPDATE", "EVENT_DELETE", "EVENT_PUBLISH",
                        "ATTENDEE_LIST", "ATTENDEE_EXPORT"));

        createRole("ORGANIZER_WELFARE_STAFF", "Personal de Bienestar Universitario organizador de eventos",
                List.of("EVENT_READ", "EVENT_CREATE", "EVENT_UPDATE", "EVENT_DELETE", "EVENT_PUBLISH",
                        "ATTENDEE_LIST", "ATTENDEE_EXPORT", "STATS_READ"));

        createRole("STUDENT", "Estudiante de la universidad",
                List.of("EVENT_READ", "ENROLLMENT_CREATE", "ENROLLMENT_CANCEL", "ENROLLMENT_READ"));
    }

    private void createRole(String name, String description, List<String> permissionResources) {
        List<Permission> assigned = permissionRepository.findAll().stream()
                .filter(p -> permissionResources.contains(p.getResource()))
                .toList();
        roleRepository.save(Role.builder()
                .name(name)
                .description(description)
                .permissions(assigned)
                .build());
    }

    private void createUsers() {
        createUser("admin@icesi.edu.co",      "000000", "admin123", "ADMIN",                    "ADMIN");
        createUser("profesor@icesi.edu.co",   "100001", "prof123",  "ORGANIZER_PROFESSOR",      "PROFESSOR");
        createUser("lider@icesi.edu.co",      "100002", "lider123", "ORGANIZER_STUDENT_LEADER", "STUDENT_LEADER");
        createUser("bienestar@icesi.edu.co",  "100003", "bien123",  "ORGANIZER_WELFARE_STAFF",  "WELFARE_STAFF");
        createUser("estudiante@icesi.edu.co", "100004", "est123",   "STUDENT",                  "STUDENT");
    }

    private void createUser(String email, String institutionalId, String rawPassword,
            String roleName, String userType) {
        // Si ya existe no lo toca — laura y otros usuarios manuales se preservan
        if (userRepository.findByInstitutionalEmail(email).isPresent()) return;

        Role role = roleRepository.findByName(roleName).orElse(null);

        userRepository.save(AppUser.builder()
                .institutionalEmail(email)
                .institutionalId(institutionalId)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .roleId(role != null ? role.getId() : null)
                .userType(userType)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .lastLogin(null)
                .build());
    }
}
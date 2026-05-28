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
import co.icesi.UniPlan.repository.mongo.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private AppUserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) throws Exception {
                if (roleRepository.count() == 0) {
                        createRoles();
                }
                createUsers();
        }

        private void createRoles() {

                saveRole("ADMIN", "Administrador general del sistema", List.of(
                                p("USER_LIST", "Listar usuarios"),
                                p("USER_CREATE", "Crear usuarios"),
                                p("USER_EDIT", "Editar usuarios"),
                                p("USER_DELETE", "Eliminar usuarios"),
                                p("ROLE_LIST", "Listar roles"),
                                p("ROLE_CREATE", "Crear roles"),
                                p("ROLE_EDIT", "Editar roles"),
                                p("ROLE_DELETE", "Eliminar roles"),
                                p("PERMISSION_LIST", "Listar permisos"),
                                p("EVENT_READ", "Consultar eventos"),
                                p("EVENT_CREATE", "Crear eventos"),
                                p("EVENT_UPDATE", "Actualizar eventos"),
                                p("EVENT_DELETE", "Eliminar eventos"),
                                p("EVENT_PUBLISH", "Publicar eventos"),
                                p("ENROLLMENT_READ", "Consultar inscripciones"),
                                p("ENROLLMENT_CREATE", "Inscribirse a eventos"),
                                p("ENROLLMENT_CANCEL", "Cancelar inscripción"),
                                p("ATTENDEE_LIST", "Listar inscritos de un evento"),
                                p("ATTENDEE_EXPORT", "Exportar lista de inscritos en CSV"),
                                p("STATS_READ", "Consultar estadísticas de eventos")));

                saveRole("PROFESSOR", "Profesor de la universidad", List.of(
                                p("EVENT_READ", "Consultar eventos"),
                                p("ENROLLMENT_READ", "Consultar inscripciones"),
                                p("ENROLLMENT_CREATE", "Inscribirse a eventos"),
                                p("ENROLLMENT_CANCEL", "Cancelar inscripción")));

                saveRole("ORGANIZER_PROFESSOR", "Profesor organizador de eventos", List.of(
                                p("EVENT_READ", "Consultar eventos"),
                                p("EVENT_CREATE", "Crear eventos"),
                                p("EVENT_UPDATE", "Actualizar eventos"),
                                p("EVENT_DELETE", "Eliminar eventos"),
                                p("EVENT_PUBLISH", "Publicar eventos"),
                                p("ATTENDEE_LIST", "Listar inscritos de un evento"),
                                p("ATTENDEE_EXPORT", "Exportar lista de inscritos en CSV")));

                saveRole("ORGANIZER_STUDENT_LEADER", "Líder estudiantil organizador de eventos", List.of(
                                p("EVENT_READ", "Consultar eventos"),
                                p("EVENT_CREATE", "Crear eventos"),
                                p("EVENT_UPDATE", "Actualizar eventos"),
                                p("EVENT_DELETE", "Eliminar eventos"),
                                p("EVENT_PUBLISH", "Publicar eventos"),
                                p("ATTENDEE_LIST", "Listar inscritos de un evento"),
                                p("ATTENDEE_EXPORT", "Exportar lista de inscritos en CSV")));

                saveRole("ORGANIZER_BU_STAFF", "Personal de Bienestar Universitario organizador de eventos", List.of(
                                p("EVENT_READ", "Consultar eventos"),
                                p("EVENT_CREATE", "Crear eventos"),
                                p("EVENT_UPDATE", "Actualizar eventos"),
                                p("EVENT_DELETE", "Eliminar eventos"),
                                p("EVENT_PUBLISH", "Publicar eventos"),
                                p("ATTENDEE_LIST", "Listar inscritos de un evento"),
                                p("ATTENDEE_EXPORT", "Exportar lista de inscritos en CSV"),
                                p("STATS_READ", "Consultar estadísticas de eventos")));

                saveRole("STUDENT", "Estudiante de la universidad", List.of(
                                p("EVENT_READ", "Consultar eventos"),
                                p("ENROLLMENT_READ", "Consultar inscripciones"),
                                p("ENROLLMENT_CREATE", "Inscribirse a eventos"),
                                p("ENROLLMENT_CANCEL", "Cancelar inscripción")));
        }

        private Permission p(String resource, String action) {
                return Permission.builder()
                                .resource(resource)
                                .actions(List.of(action))
                                .build();
        }

        private void saveRole(String name, String description, List<Permission> permissions) {
                roleRepository.save(Role.builder()
                                .name(name)
                                .description(description)
                                .permissions(permissions)
                                .build());
        }

        private void createUsers() {
                saveUser("admin@icesi.edu.co", "000000", "admin123", "ADMIN", "ADMIN");
                saveUser("profesor@icesi.edu.co", "100001", "prof123", "ORGANIZER_PROFESSOR", "PROFESSOR");
                saveUser("lider@icesi.edu.co", "100002", "lider123", "ORGANIZER_STUDENT_LEADER", "STUDENT");
                saveUser("bienestar@icesi.edu.co", "100003", "bien123", "ORGANIZER_BU_STAFF", "BU_STAFF");
                saveUser("estudiante@icesi.edu.co", "100004", "est123", "STUDENT", "STUDENT");
        }

        private void saveUser(String email, String institutionalId, String rawPassword,
                        String roleName, String userType) {
                if (userRepository.findByInstitutionalEmail(email).isPresent())
                        return;

                String roleId = roleRepository.findByName(roleName)
                                .map(Role::getId)
                                .orElse(null);

                userRepository.save(AppUser.builder()
                                .institutionalEmail(email)
                                .institutionalId(institutionalId)
                                .passwordHash(passwordEncoder.encode(rawPassword))
                                .roleId(roleId)
                                .userType(userType)
                                .status("ACTIVE")
                                .createdAt(Instant.now())
                                .lastLogin(null)
                                .build());
        }
}
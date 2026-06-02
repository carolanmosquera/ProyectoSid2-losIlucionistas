package co.icesi.UniPlan.service;

import co.icesi.UniPlan.dto.AppUserRegistrationRequest;
import co.icesi.UniPlan.exception.BusinessException;
import co.icesi.UniPlan.exception.ResourceNotFoundException;
import co.icesi.UniPlan.model.Employee;
import co.icesi.UniPlan.model.Student;
import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.model.mongo.Role;
import co.icesi.UniPlan.repository.EmployeeRepository;
import co.icesi.UniPlan.repository.StudentRepository;
import co.icesi.UniPlan.repository.mongo.AppUserRepository;
import co.icesi.UniPlan.repository.mongo.RoleRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.icesi.UniPlan.model.mongo.LeaderRequest;
import co.icesi.UniPlan.repository.mongo.LeaderRequestRepository;

@Service
public class AppUserService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String USER_TYPE_STUDENT = "STUDENT";
    private static final String USER_TYPE_PROFESSOR = "PROFESSOR";
    private static final String USER_TYPE_ADMIN = "ADMIN";
    private static final String USER_TYPE_INSTRUCTOR = "INSTRUCTOR";
    private static final String USER_TYPE_BU_STAFF   = "BU_STAFF";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final LeaderRequestRepository leaderRequestRepository;

    public AppUserService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            StudentRepository studentRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            LeaderRequestRepository leaderRequestRepository) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.leaderRequestRepository = leaderRequestRepository;
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    public AppUser findById(String id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    public AppUser registerAppUser(AppUserRegistrationRequest request, boolean requestLeader) {

        Optional<Student> studentOpt = studentRepository.findById(request.institutionalId())
                .or(() -> studentRepository.findByEmail(request.institutionalEmail()));

        if (studentOpt.isEmpty()) {
            // Caso: Empleado
            Employee employee = employeeRepository.findById(request.institutionalId())
                    .or(() -> employeeRepository.findByEmail(request.institutionalEmail()))
                    .orElseThrow(() -> new BusinessException("El usuario no pertenece a la institución educativa ICESI"));

            if (!employee.getId().equals(request.institutionalId())
                    || !employee.getEmail().equalsIgnoreCase(request.institutionalEmail())) {
                throw new BusinessException("Las credenciales ingresadas no coinciden con las institucionales");
            }

            // Si se solicitó líder pero el usuario no es estudiante → lanzar excepción
            if (requestLeader) {
                throw new BusinessException("Solo los estudiantes pueden solicitar ser líderes de evento");
            }

            String userType = switch (employee.getEmployeeType()) {
                case "Administrativo" -> USER_TYPE_ADMIN;
                case "Instructor" -> USER_TYPE_INSTRUCTOR;
                default -> USER_TYPE_PROFESSOR;
            };
            return registerValidatedUser(request, userType);
        } else {
            // Caso: Estudiante
            Student student = studentOpt.get();
            if (!student.getId().equals(request.institutionalId())
                    || !student.getEmail().equalsIgnoreCase(request.institutionalEmail())) {
                throw new BusinessException("Las credenciales ingresadas no coinciden con las institucionales");
            }

            AppUser savedUser = registerValidatedUser(request, USER_TYPE_STUDENT);

            // Si marcó la casilla de líder, crear solicitud pendiente
            if (requestLeader) {
                if (leaderRequestRepository.existsByAppUserIdAndStatus(savedUser.getId(), "PENDING")) {
                    throw new BusinessException("Ya tienes una solicitud de liderazgo pendiente");
                }
                leaderRequestRepository.save(LeaderRequest.builder()
                        .appUserId(savedUser.getId())
                        .institutionalId(savedUser.getInstitutionalId())
                        .institutionalEmail(savedUser.getInstitutionalEmail())
                        .status("PENDING")
                        .requestedAt(Instant.now())
                        .build());
            }
            return savedUser;
        }
    }

    public AppUser registerOrganizer(AppUserRegistrationRequest request) {
        boolean isInstitutionalEmployee = employeeRepository.findById(request.institutionalId())
                .map(Employee::getEmail)
                .filter(email -> email.equalsIgnoreCase(request.institutionalEmail()))
                .isPresent();
        boolean isInstitutionalStudent = studentRepository.findById(request.institutionalId())
                .map(Student::getEmail)
                .filter(email -> email.equalsIgnoreCase(request.institutionalEmail()))
                .isPresent();

        if (!isInstitutionalEmployee && !isInstitutionalStudent) {
            throw new BusinessException("El organizador no existe en la base institucional");
        }

        String userType = blankToDefault(request.userType(), "organizer");
        return registerValidatedUser(request, userType);
    }

    public AppUser updateStatus(String id, String status) {
        AppUser appUser = findById(id);
        appUser.setStatus(status);
        return appUserRepository.save(appUser);
    }

    private AppUser registerValidatedUser(AppUserRegistrationRequest request, String userType) {
        if (appUserRepository.existsByInstitutionalId(request.institutionalId())) {
            throw new BusinessException("Ya existe un usuario con ese codigo institucional");
        }
        if (appUserRepository.existsByInstitutionalEmail(request.institutionalEmail())) {
            throw new BusinessException("Ya existe un usuario con ese correo institucional");
        }

        String roleId = resolveRoleId(request.roleId(), userType);
        AppUser appUser = AppUser.builder()
                .institutionalId(request.institutionalId())
                .institutionalEmail(request.institutionalEmail())
                .passwordHash(passwordEncoder.encode(request.passwordHash()))
                .roleId(roleId)
                .userType(userType)
                .status(STATUS_ACTIVE)
                .createdAt(Instant.now())
                .build();
        return appUserRepository.save(appUser);
    }

    private String resolveRoleId(String roleId, String roleName) {
        if (roleId != null && !roleId.isBlank()) {
            return roleRepository.findById(roleId)
                    .map(Role::getId)
                    .orElseThrow(() -> new BusinessException("El rol indicado no existe"));
        }
        return roleRepository.findByName(roleName)
                .map(Role::getId)
                .orElse(null);
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    // Método que llama el admin para aprobar
    public AppUser approveLeaderRequest(String requestId, String adminEmail) {
        LeaderRequest req = leaderRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada: " + requestId));

        if (!"PENDING".equals(req.getStatus())) {
            throw new BusinessException("La solicitud ya fue procesada");
        }

        // Actualizar usuario
        AppUser user = appUserRepository.findById(req.getAppUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String roleId = roleRepository.findByName("ORGANIZER_STUDENT_LEADER")
                .map(co.icesi.UniPlan.model.mongo.Role::getId)
                .orElse(null);

        user.setUserType("ORGANIZER_STUDENT_LEADER");
        user.setRoleId(roleId);
        appUserRepository.save(user);

        // Cerrar solicitud
        req.setStatus("APPROVED");
        req.setResolvedAt(Instant.now());
        req.setResolvedBy(adminEmail);
        leaderRequestRepository.save(req);

        return user;
    }

    // Método para rechazar
    public void rejectLeaderRequest(String requestId, String adminEmail) {
        LeaderRequest req = leaderRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada: " + requestId));

        if (!"PENDING".equals(req.getStatus())) {
            throw new BusinessException("La solicitud ya fue procesada");
        }

        req.setStatus("REJECTED");
        req.setResolvedAt(Instant.now());
        req.setResolvedBy(adminEmail);
        leaderRequestRepository.save(req);
    }

    public List<LeaderRequest> findPendingLeaderRequests() {
        return leaderRequestRepository.findByStatus("PENDING");
    }


}

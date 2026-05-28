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
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class AppUserService {

    private static final String STATUS_ACTIVE = "active";
    private static final String USER_TYPE_STUDENT = "STUDENT";
    private static final String USER_TYPE_PROFESSOR = "PROFESSOR";
    private static final String USER_TYPE_ADMIN = "ADMIN";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;

    public AppUserService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            StudentRepository studentRepository,
            EmployeeRepository employeeRepository) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    public AppUser findById(String id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    public AppUser registerAppUser(AppUserRegistrationRequest request) {

        Optional<Student> studentOpt = studentRepository.findById(request.institutionalId())
                .or(() -> studentRepository.findByEmail(request.institutionalEmail()));

        if (studentOpt.isEmpty()) {

            Employee employee = employeeRepository.findById(request.institutionalId())
                    .or(() -> employeeRepository.findByEmail(request.institutionalEmail()))
                    .orElseThrow(
                            () -> new BusinessException("El usuario no pertenece a la institución educativa ICESI"));

            if (!employee.getId().equals(request.institutionalId())
                    || !employee.getEmail().equalsIgnoreCase(request.institutionalEmail())) {
                throw new BusinessException("Las credenciales ingresadas no coincide con las institucionales");
            }

            if (employee.getEmployeeType().equals("Administrativo")) {

                return registerValidatedUser(request, USER_TYPE_ADMIN);

            }

            return registerValidatedUser(request, USER_TYPE_PROFESSOR);

        } else {

            Student student = studentOpt.get();

            if (!student.getId().equals(request.institutionalId())
                    || !student.getEmail().equalsIgnoreCase(request.institutionalEmail())) {
                throw new BusinessException("Las credenciales ingresadas no coincide con las institucionales");
            }

        }

        return registerValidatedUser(request, USER_TYPE_STUDENT);
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
                .passwordHash(request.passwordHash())
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
}

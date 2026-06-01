error id: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/AppUserService.java
file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/AppUserService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[41,52]

error in qdox parser
file content:
```java
offset: 1732
uri: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/AppUserService.java
text:
```scala
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
    LeaderRequestRepository leaderRequestRepository)@@

    public AppUserService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            StudentRepository studentRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
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
 !employee.getEmail().equalsIgnoreCase(request.institutionalEmail())) {
                throw new BusinessException("Las credenciales ingresadas no coincide con las institucionales");
            }

            return switch (employee.getEmployeeType()) {
                case "Administrativo" -> registerValidatedUser(request, USER_TYPE_ADMIN);
                case "Instructor"     -> registerValidatedUser(request, USER_TYPE_INSTRUCTOR);
                default               -> registerValidatedUser(request, USER_TYPE_PROFESSOR); // Docente y cualquier otro
            };

        } else {

            Student student = studentOpt.get();

            if (!student.getId().equals(request.institutionalId())
 !student.getEmail().equalsIgnoreCase(request.institutionalEmail())) {
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

QDox parse error in file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/service/AppUserService.java
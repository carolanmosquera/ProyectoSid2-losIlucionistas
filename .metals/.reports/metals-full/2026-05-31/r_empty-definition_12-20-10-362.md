error id: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/config/CustomUserDetailsService.java:
file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/config/CustomUserDetailsService.java
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2025
uri: file:///C:/Users/carol/Desktop/UNIVERSIDAD/SID2/proyectoIntegradorSid/ProyectoSid2-losIlucionistas/src/main/java/co/icesi/UniPlan/config/CustomUserDetailsService.java
text:
```scala
package co.icesi.UniPlan.config;

import co.icesi.UniPlan.model.User;
import co.icesi.UniPlan.model.mongo.AppUser;
import co.icesi.UniPlan.repository.mongo.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Nullable
    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(
            @Autowired(required = false) AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscar en MongoDB por institutionalEmail o institutionalId
        if (appUserRepository != null) {
            AppUser mongoUser = appUserRepository.findByInstitutionalEmail(username)
                    .orElse(null);

            if (mongoUser == null) {
                mongoUser = appUserRepository.findByInstitutionalId(username)
                        .orElse(null);
            }

            if (mongoUser != null) {
                return buildFromMongoUser(mongoUser);
            }
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

    private UserDetails buildFromMongoUser(AppUser user) {
        String role = user.getUserType() != null
                ? user.getUserType().toUpperCase()
                : "STUDENT";

        // Mapear tipos de empleados organizadores al rol correcto
        String role = switch@@ (userType) {
            case "PROFESSOR"   -> "ORGANIZER_PROFESSOR";
            case "INSTRUCTOR"  -> "ORGANIZER_PROFESSOR";   // mismos permisos que profesor organizador
            case "BU_STAFF"    -> "ORGANIZER_BU_STAFF";
            case "ADMIN"       -> "ADMIN";
            default            -> userType; // STUDENT, ORGANIZER_STUDENT_LEADER, etc. pasan directo
        };
        return new org.springframework.security.core.userdetails.User(
                // Usar el email como principal para usuarios MongoDB
                user.getInstitutionalEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 
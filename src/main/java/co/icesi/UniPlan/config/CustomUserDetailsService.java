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
        return new org.springframework.security.core.userdetails.User(
                // Usar el email como principal para usuarios MongoDB
                user.getInstitutionalEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
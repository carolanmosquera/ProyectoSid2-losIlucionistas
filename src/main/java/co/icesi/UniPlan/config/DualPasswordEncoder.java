package co.icesi.UniPlan.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Encoder dual: verifica BCrypt para usuarios MongoDB (DataInitializer)
 * y texto plano para usuarios PostgreSQL (datos de ejemplo).
 */
public class DualPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;

        // Si el hash es BCrypt, usar BCrypt
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }

        // Si no, comparar como texto plano (usuarios PostgreSQL de prueba)
        return rawPassword.toString().equals(encodedPassword);
    }
}
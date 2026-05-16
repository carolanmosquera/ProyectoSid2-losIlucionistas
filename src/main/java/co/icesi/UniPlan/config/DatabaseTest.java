package co.icesi.UniPlan.config;

import co.icesi.UniPlan.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseTest {

    @Bean
    CommandLineRunner testConnection(StudentRepository studentRepository) {
        return args -> {

            System.out.println("=================================");
            System.out.println("Probando conexión con la base...");
            System.out.println("Cantidad de estudiantes: " + studentRepository.count());
            System.out.println("=================================");

        };
    }

}
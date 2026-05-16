package co.icesi.UniPlan.controller;

import co.icesi.UniPlan.repository.StudentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final StudentRepository studentRepository;

    public TestController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/test-db")
    public String testDB() {
        return "Students in DB: " + studentRepository.count();
    }

}
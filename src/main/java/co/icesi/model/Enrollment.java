package co.icesi.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ENROLLMENTS")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "NRC")
    private Group group;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "status")
    private String status;

}
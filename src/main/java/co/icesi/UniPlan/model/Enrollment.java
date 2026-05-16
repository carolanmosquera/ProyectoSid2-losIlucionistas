package co.icesi.UniPlan.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ENROLLMENTS")
public class Enrollment {

    @EmbeddedId
    private EnrollmentId id;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @MapsId("nrc")
    @JoinColumn(name = "NRC")
    private Group group;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "status")
    private String status;

}
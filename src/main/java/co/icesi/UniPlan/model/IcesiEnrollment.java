package co.icesi.UniPlan.model;

import com.mongodb.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity
@Table(name = "ENROLLMENTS", schema = "icesi")
@Immutable
public class IcesiEnrollment {

    @Id
    private Long id;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "semester")
    private String semester;
    
}

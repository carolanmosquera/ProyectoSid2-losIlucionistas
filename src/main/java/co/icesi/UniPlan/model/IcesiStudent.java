package co.icesi.UniPlan.model;

import com.mongodb.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// Solo lectura — inmutable desde UniPlan
@Data 
@Entity
@Table(name = "STUDENTS", schema = "icesi")
@Immutable 
public class IcesiStudent {

    @Id
    @Column(name = "institutional_id")
    private String institutionalId;   // "A00405277"

    @Column(name = "email")
    private String email;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "current_semester")
    private Integer currentSemester;

}

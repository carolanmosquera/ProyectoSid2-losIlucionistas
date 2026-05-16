package co.icesi.UniPlan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EMPLOYEES")
public class Employee {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "employee_type")
    private String employeeType;

    @ManyToOne
    @JoinColumn(name = "faculty_code")
    private Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "campus_code")
    private Campus campus;

    @ManyToOne
    @JoinColumn(name = "birth_place_code")
    private City birthPlace;

}
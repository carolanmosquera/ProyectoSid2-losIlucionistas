package co.icesi.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "STUDENTS")
public class Student {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "birth_place_code")
    private City birthPlace;

    @ManyToOne
    @JoinColumn(name = "campus_code")
    private Campus campus;

}
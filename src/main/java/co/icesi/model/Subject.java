package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "SUBJECTS")
public class Subject {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "program_code")
    private Program program;

    public Subject() {
    }

}
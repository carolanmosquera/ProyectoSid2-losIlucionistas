package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PROGRAMS")
public class Program {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "area_code")
    private Area area;

    public Program() {
    }

}
package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "AREAS")
public class Area {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "faculty_code")
    private Faculty faculty;

    @Column(name = "coordinator_id")
    private String coordinatorId;

    public Area() {
    }

}
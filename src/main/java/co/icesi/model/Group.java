package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "GROUPS")
public class Group {

    @Id
    @Column(name = "NRC")
    private String nrc;

    @Column(name = "number")
    private Integer number;

    @Column(name = "semester")
    private String semester;

    @ManyToOne
    @JoinColumn(name = "subject_code")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Employee professor;

}
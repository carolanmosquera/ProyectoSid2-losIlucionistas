package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "FACULTIES")
public class Faculty {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "dean_id")
    private String deanId;

    public Faculty() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
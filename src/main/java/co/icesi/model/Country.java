package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "COUNTRIES")
public class Country {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name", nullable = false)
    private String name;

    public Country() {
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
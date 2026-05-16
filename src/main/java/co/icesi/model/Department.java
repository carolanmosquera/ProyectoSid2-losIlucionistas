package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DEPARTMENTS")
public class Department {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "country_code")
    private Country country;

    public Department() {
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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

}
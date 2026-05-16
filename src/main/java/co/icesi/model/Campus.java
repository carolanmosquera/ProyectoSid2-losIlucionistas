package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CAMPUSES")
public class Campus {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "city_code")
    private City city;

    public Campus() {
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

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

}
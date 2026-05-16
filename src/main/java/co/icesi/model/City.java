package co.icesi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CITIES")
public class City {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "dept_code")
    private Department department;

    public City() {
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

}
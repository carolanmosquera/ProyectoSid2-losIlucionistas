package co.icesi.UniPlan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EnrollmentId implements Serializable {

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "NRC")
    private String nrc;

    public EnrollmentId() {
    }

    public EnrollmentId(String studentId, String nrc) {
        this.studentId = studentId;
        this.nrc = nrc;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EnrollmentId))
            return false;
        EnrollmentId that = (EnrollmentId) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(nrc, that.nrc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, nrc);
    }
}
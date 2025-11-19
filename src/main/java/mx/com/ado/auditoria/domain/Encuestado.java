package mx.com.ado.auditoria.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Encuestado.
 */
@Entity
@Table(name = "encuestado")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Encuestado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "clave_empleado")
    private String claveEmpleado;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "encuestado")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "encuestado", "encuesta" }, allowSetters = true)
    private Set<AplicacionEncuesta> aplicacionEncuestas = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Encuestado id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Encuestado nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return this.fechaNacimiento;
    }

    public Encuestado fechaNacimiento(LocalDate fechaNacimiento) {
        this.setFechaNacimiento(fechaNacimiento);
        return this;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getClaveEmpleado() {
        return this.claveEmpleado;
    }

    public Encuestado claveEmpleado(String claveEmpleado) {
        this.setClaveEmpleado(claveEmpleado);
        return this;
    }

    public void setClaveEmpleado(String claveEmpleado) {
        this.claveEmpleado = claveEmpleado;
    }

    public Set<AplicacionEncuesta> getAplicacionEncuestas() {
        return this.aplicacionEncuestas;
    }

    public void setAplicacionEncuestas(Set<AplicacionEncuesta> aplicacionEncuestas) {
        if (this.aplicacionEncuestas != null) {
            this.aplicacionEncuestas.forEach(i -> i.setEncuestado(null));
        }
        if (aplicacionEncuestas != null) {
            aplicacionEncuestas.forEach(i -> i.setEncuestado(this));
        }
        this.aplicacionEncuestas = aplicacionEncuestas;
    }

    public Encuestado aplicacionEncuestas(Set<AplicacionEncuesta> aplicacionEncuestas) {
        this.setAplicacionEncuestas(aplicacionEncuestas);
        return this;
    }

    public Encuestado addAplicacionEncuesta(AplicacionEncuesta aplicacionEncuesta) {
        this.aplicacionEncuestas.add(aplicacionEncuesta);
        aplicacionEncuesta.setEncuestado(this);
        return this;
    }

    public Encuestado removeAplicacionEncuesta(AplicacionEncuesta aplicacionEncuesta) {
        this.aplicacionEncuestas.remove(aplicacionEncuesta);
        aplicacionEncuesta.setEncuestado(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Encuestado)) {
            return false;
        }
        return getId() != null && getId().equals(((Encuestado) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Encuestado{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", fechaNacimiento='" + getFechaNacimiento() + "'" +
            ", claveEmpleado='" + getClaveEmpleado() + "'" +
            "}";
    }
}

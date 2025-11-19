package mx.com.ado.auditoria.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AplicacionEncuesta.
 */
@Entity
@Table(name = "aplicacion_encuesta")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AplicacionEncuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "enlace_unico")
    private String enlaceUnico;

    @Column(name = "fecha_aplicacion")
    private LocalDate fechaAplicacion;

    @Lob
    @Column(name = "respuesta_encuesta")
    private String respuestaEncuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "aplicacionEncuestas" }, allowSetters = true)
    private Encuestado encuestado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "aplicacionEncuestas" }, allowSetters = true)
    private Encuesta encuesta;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AplicacionEncuesta id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnlaceUnico() {
        return this.enlaceUnico;
    }

    public AplicacionEncuesta enlaceUnico(String enlaceUnico) {
        this.setEnlaceUnico(enlaceUnico);
        return this;
    }

    public void setEnlaceUnico(String enlaceUnico) {
        this.enlaceUnico = enlaceUnico;
    }

    public LocalDate getFechaAplicacion() {
        return this.fechaAplicacion;
    }

    public AplicacionEncuesta fechaAplicacion(LocalDate fechaAplicacion) {
        this.setFechaAplicacion(fechaAplicacion);
        return this;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public String getRespuestaEncuesta() {
        return this.respuestaEncuesta;
    }

    public AplicacionEncuesta respuestaEncuesta(String respuestaEncuesta) {
        this.setRespuestaEncuesta(respuestaEncuesta);
        return this;
    }

    public void setRespuestaEncuesta(String respuestaEncuesta) {
        this.respuestaEncuesta = respuestaEncuesta;
    }

    public Encuestado getEncuestado() {
        return this.encuestado;
    }

    public void setEncuestado(Encuestado encuestado) {
        this.encuestado = encuestado;
    }

    public AplicacionEncuesta encuestado(Encuestado encuestado) {
        this.setEncuestado(encuestado);
        return this;
    }

    public Encuesta getEncuesta() {
        return this.encuesta;
    }

    public void setEncuesta(Encuesta encuesta) {
        this.encuesta = encuesta;
    }

    public AplicacionEncuesta encuesta(Encuesta encuesta) {
        this.setEncuesta(encuesta);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AplicacionEncuesta)) {
            return false;
        }
        return getId() != null && getId().equals(((AplicacionEncuesta) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AplicacionEncuesta{" +
            "id=" + getId() +
            ", enlaceUnico='" + getEnlaceUnico() + "'" +
            ", fechaAplicacion='" + getFechaAplicacion() + "'" +
            ", respuestaEncuesta='" + getRespuestaEncuesta() + "'" +
            "}";
    }
}

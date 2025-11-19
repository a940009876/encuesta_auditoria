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
 * A Encuesta.
 */
@Entity
@Table(name = "encuesta")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Encuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "vigencia_desde")
    private LocalDate vigenciaDesde;

    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;

    @Lob
    @Column(name = "structura_json")
    private String structuraJson;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "encuesta")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "encuestado", "encuesta" }, allowSetters = true)
    private Set<AplicacionEncuesta> aplicacionEncuestas = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Encuesta id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Encuesta nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getVigenciaDesde() {
        return this.vigenciaDesde;
    }

    public Encuesta vigenciaDesde(LocalDate vigenciaDesde) {
        this.setVigenciaDesde(vigenciaDesde);
        return this;
    }

    public void setVigenciaDesde(LocalDate vigenciaDesde) {
        this.vigenciaDesde = vigenciaDesde;
    }

    public LocalDate getVigenciaHasta() {
        return this.vigenciaHasta;
    }

    public Encuesta vigenciaHasta(LocalDate vigenciaHasta) {
        this.setVigenciaHasta(vigenciaHasta);
        return this;
    }

    public void setVigenciaHasta(LocalDate vigenciaHasta) {
        this.vigenciaHasta = vigenciaHasta;
    }

    public String getStructuraJson() {
        return this.structuraJson;
    }

    public Encuesta structuraJson(String structuraJson) {
        this.setStructuraJson(structuraJson);
        return this;
    }

    public void setStructuraJson(String structuraJson) {
        this.structuraJson = structuraJson;
    }

    public Set<AplicacionEncuesta> getAplicacionEncuestas() {
        return this.aplicacionEncuestas;
    }

    public void setAplicacionEncuestas(Set<AplicacionEncuesta> aplicacionEncuestas) {
        if (this.aplicacionEncuestas != null) {
            this.aplicacionEncuestas.forEach(i -> i.setEncuesta(null));
        }
        if (aplicacionEncuestas != null) {
            aplicacionEncuestas.forEach(i -> i.setEncuesta(this));
        }
        this.aplicacionEncuestas = aplicacionEncuestas;
    }

    public Encuesta aplicacionEncuestas(Set<AplicacionEncuesta> aplicacionEncuestas) {
        this.setAplicacionEncuestas(aplicacionEncuestas);
        return this;
    }

    public Encuesta addAplicacionEncuesta(AplicacionEncuesta aplicacionEncuesta) {
        this.aplicacionEncuestas.add(aplicacionEncuesta);
        aplicacionEncuesta.setEncuesta(this);
        return this;
    }

    public Encuesta removeAplicacionEncuesta(AplicacionEncuesta aplicacionEncuesta) {
        this.aplicacionEncuestas.remove(aplicacionEncuesta);
        aplicacionEncuesta.setEncuesta(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Encuesta)) {
            return false;
        }
        return getId() != null && getId().equals(((Encuesta) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Encuesta{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", vigenciaDesde='" + getVigenciaDesde() + "'" +
            ", vigenciaHasta='" + getVigenciaHasta() + "'" +
            ", structuraJson='" + getStructuraJson() + "'" +
            "}";
    }
}

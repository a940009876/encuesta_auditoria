package mx.com.ado.auditoria.repository;

import java.time.LocalDate;
import java.util.List;
import mx.com.ado.auditoria.domain.Encuesta;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Encuesta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {
    @Query("select e from Encuesta e where e.vigenciaHasta > :fechaActual order by e.id desc")
    List<Encuesta> findVigentesOrderByIdDesc(@Param("fechaActual") LocalDate fechaActual);
}

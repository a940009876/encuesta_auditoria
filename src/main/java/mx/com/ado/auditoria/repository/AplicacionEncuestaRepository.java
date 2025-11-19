package mx.com.ado.auditoria.repository;

import java.util.List;
import java.util.Optional;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AplicacionEncuesta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AplicacionEncuestaRepository extends JpaRepository<AplicacionEncuesta, Long> {
    @EntityGraph(attributePaths = { "encuesta", "encuestado" })
    @Query("select a from AplicacionEncuesta a where a.enlaceUnico = :enlaceUnico")
    Optional<AplicacionEncuesta> findByEnlaceUnico(@Param("enlaceUnico") String enlaceUnico);

    @Query("select a from AplicacionEncuesta a where a.encuestado.id = :encuestadoId and a.encuesta.id = :encuestaId")
    Optional<AplicacionEncuesta> findByEncuestadoAndEncuesta(@Param("encuestadoId") Long encuestadoId, @Param("encuestaId") Long encuestaId);

    @EntityGraph(attributePaths = { "encuesta", "encuestado" })
    @Query("select a from AplicacionEncuesta a where a.encuesta.id = :encuestaId and a.fechaAplicacion is not null order by a.fechaAplicacion desc")
    List<AplicacionEncuesta> findCompletadasByEncuestaId(@Param("encuestaId") Long encuestaId);
}

package mx.com.ado.auditoria.repository;

import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuestado;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Encuestado entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EncuestadoRepository extends JpaRepository<Encuestado, Long> {
    Optional<Encuestado> findByNombreAndClaveEmpleado(String nombre, String claveEmpleado);
}

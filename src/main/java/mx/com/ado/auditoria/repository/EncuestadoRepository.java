package mx.com.ado.auditoria.repository;

import mx.com.ado.auditoria.domain.Encuestado;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Encuestado entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EncuestadoRepository extends JpaRepository<Encuestado, Long> {}

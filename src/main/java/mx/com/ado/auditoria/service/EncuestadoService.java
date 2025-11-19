package mx.com.ado.auditoria.service;

import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuestado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link mx.com.ado.auditoria.domain.Encuestado}.
 */
public interface EncuestadoService {
    /**
     * Save a encuestado.
     *
     * @param encuestado the entity to save.
     * @return the persisted entity.
     */
    Encuestado save(Encuestado encuestado);

    /**
     * Updates a encuestado.
     *
     * @param encuestado the entity to update.
     * @return the persisted entity.
     */
    Encuestado update(Encuestado encuestado);

    /**
     * Partially updates a encuestado.
     *
     * @param encuestado the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Encuestado> partialUpdate(Encuestado encuestado);

    /**
     * Get all the encuestados.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Encuestado> findAll(Pageable pageable);

    /**
     * Get the "id" encuestado.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Encuestado> findOne(Long id);

    /**
     * Delete the "id" encuestado.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}

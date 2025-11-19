package mx.com.ado.auditoria.service;

import java.util.List;
import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link mx.com.ado.auditoria.domain.Encuesta}.
 */
public interface EncuestaService {
    /**
     * Save a encuesta.
     *
     * @param encuesta the entity to save.
     * @return the persisted entity.
     */
    Encuesta save(Encuesta encuesta);

    /**
     * Updates a encuesta.
     *
     * @param encuesta the entity to update.
     * @return the persisted entity.
     */
    Encuesta update(Encuesta encuesta);

    /**
     * Partially updates a encuesta.
     *
     * @param encuesta the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Encuesta> partialUpdate(Encuesta encuesta);

    /**
     * Get all the encuestas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Encuesta> findAll(Pageable pageable);

    /**
     * Get the "id" encuesta.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Encuesta> findOne(Long id);

    /**
     * Delete the "id" encuesta.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Get all vigentes encuestas (vigenciaHasta > current date) ordered by id desc.
     *
     * @return the list of vigentes encuestas.
     */
    List<Encuesta> findVigentesOrderByIdDesc();
}

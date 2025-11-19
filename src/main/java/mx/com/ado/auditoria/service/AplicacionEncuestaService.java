package mx.com.ado.auditoria.service;

import java.util.List;
import java.util.Optional;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link mx.com.ado.auditoria.domain.AplicacionEncuesta}.
 */
public interface AplicacionEncuestaService {
    /**
     * Save a aplicacionEncuesta.
     *
     * @param aplicacionEncuesta the entity to save.
     * @return the persisted entity.
     */
    AplicacionEncuesta save(AplicacionEncuesta aplicacionEncuesta);

    /**
     * Updates a aplicacionEncuesta.
     *
     * @param aplicacionEncuesta the entity to update.
     * @return the persisted entity.
     */
    AplicacionEncuesta update(AplicacionEncuesta aplicacionEncuesta);

    /**
     * Partially updates a aplicacionEncuesta.
     *
     * @param aplicacionEncuesta the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AplicacionEncuesta> partialUpdate(AplicacionEncuesta aplicacionEncuesta);

    /**
     * Get all the aplicacionEncuestas.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AplicacionEncuesta> findAll(Pageable pageable);

    /**
     * Get the "id" aplicacionEncuesta.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AplicacionEncuesta> findOne(Long id);

    /**
     * Delete the "id" aplicacionEncuesta.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Get the "enlaceUnico" aplicacionEncuesta.
     *
     * @param enlaceUnico the enlaceUnico of the entity.
     * @return the entity.
     */
    Optional<AplicacionEncuesta> findByEnlaceUnico(String enlaceUnico);

    /**
     * Generate AplicacionEncuesta records for all Encuestados with the given Encuesta.
     *
     * @param encuestaId the id of the encuesta.
     * @return the number of enlaces created.
     */
    int generarEnlacesParaEncuestados(Long encuestaId);

    /**
     * Get all completed AplicacionEncuesta for a given Encuesta.
     *
     * @param encuestaId the id of the encuesta.
     * @return the list of completed aplicacionEncuestas.
     */
    List<AplicacionEncuesta> findCompletadasByEncuestaId(Long encuestaId);
}

package mx.com.ado.auditoria.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.repository.AplicacionEncuestaRepository;
import mx.com.ado.auditoria.service.AplicacionEncuestaService;
import mx.com.ado.auditoria.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link mx.com.ado.auditoria.domain.AplicacionEncuesta}.
 */
@RestController
@RequestMapping("/api/aplicacion-encuestas")
public class AplicacionEncuestaResource {

    private static final Logger LOG = LoggerFactory.getLogger(AplicacionEncuestaResource.class);

    private static final String ENTITY_NAME = "aplicacionEncuesta";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AplicacionEncuestaService aplicacionEncuestaService;

    private final AplicacionEncuestaRepository aplicacionEncuestaRepository;

    public AplicacionEncuestaResource(
        AplicacionEncuestaService aplicacionEncuestaService,
        AplicacionEncuestaRepository aplicacionEncuestaRepository
    ) {
        this.aplicacionEncuestaService = aplicacionEncuestaService;
        this.aplicacionEncuestaRepository = aplicacionEncuestaRepository;
    }

    /**
     * {@code POST  /aplicacion-encuestas} : Create a new aplicacionEncuesta.
     *
     * @param aplicacionEncuesta the aplicacionEncuesta to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new aplicacionEncuesta, or with status {@code 400 (Bad Request)} if the aplicacionEncuesta has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AplicacionEncuesta> createAplicacionEncuesta(@RequestBody AplicacionEncuesta aplicacionEncuesta)
        throws URISyntaxException {
        LOG.debug("REST request to save AplicacionEncuesta : {}", aplicacionEncuesta);
        if (aplicacionEncuesta.getId() != null) {
            throw new BadRequestAlertException("A new aplicacionEncuesta cannot already have an ID", ENTITY_NAME, "idexists");
        }
        aplicacionEncuesta = aplicacionEncuestaService.save(aplicacionEncuesta);
        return ResponseEntity.created(new URI("/api/aplicacion-encuestas/" + aplicacionEncuesta.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, aplicacionEncuesta.getId().toString()))
            .body(aplicacionEncuesta);
    }

    /**
     * {@code PUT  /aplicacion-encuestas/:id} : Updates an existing aplicacionEncuesta.
     *
     * @param id the id of the aplicacionEncuesta to save.
     * @param aplicacionEncuesta the aplicacionEncuesta to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated aplicacionEncuesta,
     * or with status {@code 400 (Bad Request)} if the aplicacionEncuesta is not valid,
     * or with status {@code 500 (Internal Server Error)} if the aplicacionEncuesta couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AplicacionEncuesta> updateAplicacionEncuesta(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AplicacionEncuesta aplicacionEncuesta
    ) throws URISyntaxException {
        LOG.debug("REST request to update AplicacionEncuesta : {}, {}", id, aplicacionEncuesta);
        if (aplicacionEncuesta.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, aplicacionEncuesta.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!aplicacionEncuestaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        aplicacionEncuesta = aplicacionEncuestaService.update(aplicacionEncuesta);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, aplicacionEncuesta.getId().toString()))
            .body(aplicacionEncuesta);
    }

    /**
     * {@code PATCH  /aplicacion-encuestas/:id} : Partial updates given fields of an existing aplicacionEncuesta, field will ignore if it is null
     *
     * @param id the id of the aplicacionEncuesta to save.
     * @param aplicacionEncuesta the aplicacionEncuesta to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated aplicacionEncuesta,
     * or with status {@code 400 (Bad Request)} if the aplicacionEncuesta is not valid,
     * or with status {@code 404 (Not Found)} if the aplicacionEncuesta is not found,
     * or with status {@code 500 (Internal Server Error)} if the aplicacionEncuesta couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AplicacionEncuesta> partialUpdateAplicacionEncuesta(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody AplicacionEncuesta aplicacionEncuesta
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AplicacionEncuesta partially : {}, {}", id, aplicacionEncuesta);
        if (aplicacionEncuesta.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, aplicacionEncuesta.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!aplicacionEncuestaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AplicacionEncuesta> result = aplicacionEncuestaService.partialUpdate(aplicacionEncuesta);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, aplicacionEncuesta.getId().toString())
        );
    }

    /**
     * {@code GET  /aplicacion-encuestas} : get all the aplicacionEncuestas.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of aplicacionEncuestas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AplicacionEncuesta>> getAllAplicacionEncuestas(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of AplicacionEncuestas");
        Page<AplicacionEncuesta> page = aplicacionEncuestaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /aplicacion-encuestas/:id} : get the "id" aplicacionEncuesta.
     *
     * @param id the id of the aplicacionEncuesta to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the aplicacionEncuesta, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AplicacionEncuesta> getAplicacionEncuesta(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AplicacionEncuesta : {}", id);
        Optional<AplicacionEncuesta> aplicacionEncuesta = aplicacionEncuestaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(aplicacionEncuesta);
    }

    /**
     * {@code DELETE  /aplicacion-encuestas/:id} : delete the "id" aplicacionEncuesta.
     *
     * @param id the id of the aplicacionEncuesta to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAplicacionEncuesta(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AplicacionEncuesta : {}", id);
        aplicacionEncuestaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

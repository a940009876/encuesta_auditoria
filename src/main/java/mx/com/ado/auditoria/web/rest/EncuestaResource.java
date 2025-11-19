package mx.com.ado.auditoria.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.repository.EncuestaRepository;
import mx.com.ado.auditoria.service.EncuestaService;
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
 * REST controller for managing {@link mx.com.ado.auditoria.domain.Encuesta}.
 */
@RestController
@RequestMapping("/api/encuestas")
public class EncuestaResource {

    private static final Logger LOG = LoggerFactory.getLogger(EncuestaResource.class);

    private static final String ENTITY_NAME = "encuesta";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EncuestaService encuestaService;

    private final EncuestaRepository encuestaRepository;

    public EncuestaResource(EncuestaService encuestaService, EncuestaRepository encuestaRepository) {
        this.encuestaService = encuestaService;
        this.encuestaRepository = encuestaRepository;
    }

    /**
     * {@code POST  /encuestas} : Create a new encuesta.
     *
     * @param encuesta the encuesta to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new encuesta, or with status {@code 400 (Bad Request)} if the encuesta has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Encuesta> createEncuesta(@RequestBody Encuesta encuesta) throws URISyntaxException {
        LOG.debug("REST request to save Encuesta : {}", encuesta);
        if (encuesta.getId() != null) {
            throw new BadRequestAlertException("A new encuesta cannot already have an ID", ENTITY_NAME, "idexists");
        }
        encuesta = encuestaService.save(encuesta);
        return ResponseEntity.created(new URI("/api/encuestas/" + encuesta.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, encuesta.getId().toString()))
            .body(encuesta);
    }

    /**
     * {@code PUT  /encuestas/:id} : Updates an existing encuesta.
     *
     * @param id the id of the encuesta to save.
     * @param encuesta the encuesta to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated encuesta,
     * or with status {@code 400 (Bad Request)} if the encuesta is not valid,
     * or with status {@code 500 (Internal Server Error)} if the encuesta couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Encuesta> updateEncuesta(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Encuesta encuesta
    ) throws URISyntaxException {
        LOG.debug("REST request to update Encuesta : {}, {}", id, encuesta);
        if (encuesta.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, encuesta.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!encuestaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        encuesta = encuestaService.update(encuesta);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, encuesta.getId().toString()))
            .body(encuesta);
    }

    /**
     * {@code PATCH  /encuestas/:id} : Partial updates given fields of an existing encuesta, field will ignore if it is null
     *
     * @param id the id of the encuesta to save.
     * @param encuesta the encuesta to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated encuesta,
     * or with status {@code 400 (Bad Request)} if the encuesta is not valid,
     * or with status {@code 404 (Not Found)} if the encuesta is not found,
     * or with status {@code 500 (Internal Server Error)} if the encuesta couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Encuesta> partialUpdateEncuesta(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Encuesta encuesta
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Encuesta partially : {}, {}", id, encuesta);
        if (encuesta.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, encuesta.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!encuestaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Encuesta> result = encuestaService.partialUpdate(encuesta);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, encuesta.getId().toString())
        );
    }

    /**
     * {@code GET  /encuestas} : get all the encuestas.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of encuestas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Encuesta>> getAllEncuestas(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Encuestas");
        Page<Encuesta> page = encuestaService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /encuestas/:id} : get the "id" encuesta.
     *
     * @param id the id of the encuesta to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the encuesta, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Encuesta> getEncuesta(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Encuesta : {}", id);
        Optional<Encuesta> encuesta = encuestaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(encuesta);
    }

    /**
     * {@code DELETE  /encuestas/:id} : delete the "id" encuesta.
     *
     * @param id the id of the encuesta to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEncuesta(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Encuesta : {}", id);
        encuestaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

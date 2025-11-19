package mx.com.ado.auditoria.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.repository.EncuestadoRepository;
import mx.com.ado.auditoria.service.EncuestadoService;
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
 * REST controller for managing {@link mx.com.ado.auditoria.domain.Encuestado}.
 */
@RestController
@RequestMapping("/api/encuestados")
public class EncuestadoResource {

    private static final Logger LOG = LoggerFactory.getLogger(EncuestadoResource.class);

    private static final String ENTITY_NAME = "encuestado";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EncuestadoService encuestadoService;

    private final EncuestadoRepository encuestadoRepository;

    public EncuestadoResource(EncuestadoService encuestadoService, EncuestadoRepository encuestadoRepository) {
        this.encuestadoService = encuestadoService;
        this.encuestadoRepository = encuestadoRepository;
    }

    /**
     * {@code POST  /encuestados} : Create a new encuestado.
     *
     * @param encuestado the encuestado to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new encuestado, or with status {@code 400 (Bad Request)} if the encuestado has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Encuestado> createEncuestado(@RequestBody Encuestado encuestado) throws URISyntaxException {
        LOG.debug("REST request to save Encuestado : {}", encuestado);
        if (encuestado.getId() != null) {
            throw new BadRequestAlertException("A new encuestado cannot already have an ID", ENTITY_NAME, "idexists");
        }
        encuestado = encuestadoService.save(encuestado);
        return ResponseEntity.created(new URI("/api/encuestados/" + encuestado.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, encuestado.getId().toString()))
            .body(encuestado);
    }

    /**
     * {@code PUT  /encuestados/:id} : Updates an existing encuestado.
     *
     * @param id the id of the encuestado to save.
     * @param encuestado the encuestado to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated encuestado,
     * or with status {@code 400 (Bad Request)} if the encuestado is not valid,
     * or with status {@code 500 (Internal Server Error)} if the encuestado couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Encuestado> updateEncuestado(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Encuestado encuestado
    ) throws URISyntaxException {
        LOG.debug("REST request to update Encuestado : {}, {}", id, encuestado);
        if (encuestado.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, encuestado.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!encuestadoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        encuestado = encuestadoService.update(encuestado);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, encuestado.getId().toString()))
            .body(encuestado);
    }

    /**
     * {@code PATCH  /encuestados/:id} : Partial updates given fields of an existing encuestado, field will ignore if it is null
     *
     * @param id the id of the encuestado to save.
     * @param encuestado the encuestado to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated encuestado,
     * or with status {@code 400 (Bad Request)} if the encuestado is not valid,
     * or with status {@code 404 (Not Found)} if the encuestado is not found,
     * or with status {@code 500 (Internal Server Error)} if the encuestado couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Encuestado> partialUpdateEncuestado(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Encuestado encuestado
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Encuestado partially : {}, {}", id, encuestado);
        if (encuestado.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, encuestado.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!encuestadoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Encuestado> result = encuestadoService.partialUpdate(encuestado);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, encuestado.getId().toString())
        );
    }

    /**
     * {@code GET  /encuestados} : get all the encuestados.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of encuestados in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Encuestado>> getAllEncuestados(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Encuestados");
        Page<Encuestado> page = encuestadoService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /encuestados/:id} : get the "id" encuestado.
     *
     * @param id the id of the encuestado to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the encuestado, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Encuestado> getEncuestado(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Encuestado : {}", id);
        Optional<Encuestado> encuestado = encuestadoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(encuestado);
    }

    /**
     * {@code DELETE  /encuestados/:id} : delete the "id" encuestado.
     *
     * @param id the id of the encuestado to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEncuestado(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Encuestado : {}", id);
        encuestadoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

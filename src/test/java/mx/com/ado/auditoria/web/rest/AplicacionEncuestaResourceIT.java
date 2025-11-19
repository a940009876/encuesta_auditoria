package mx.com.ado.auditoria.web.rest;

import static mx.com.ado.auditoria.domain.AplicacionEncuestaAsserts.*;
import static mx.com.ado.auditoria.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import mx.com.ado.auditoria.IntegrationTest;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.repository.AplicacionEncuestaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AplicacionEncuestaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AplicacionEncuestaResourceIT {

    private static final String DEFAULT_ENLACE_UNICO = "AAAAAAAAAA";
    private static final String UPDATED_ENLACE_UNICO = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_FECHA_APLICACION = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FECHA_APLICACION = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_RESPUESTA_ENCUESTA = "AAAAAAAAAA";
    private static final String UPDATED_RESPUESTA_ENCUESTA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/aplicacion-encuestas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AplicacionEncuestaRepository aplicacionEncuestaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAplicacionEncuestaMockMvc;

    private AplicacionEncuesta aplicacionEncuesta;

    private AplicacionEncuesta insertedAplicacionEncuesta;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AplicacionEncuesta createEntity() {
        return new AplicacionEncuesta()
            .enlaceUnico(DEFAULT_ENLACE_UNICO)
            .fechaAplicacion(DEFAULT_FECHA_APLICACION)
            .respuestaEncuesta(DEFAULT_RESPUESTA_ENCUESTA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AplicacionEncuesta createUpdatedEntity() {
        return new AplicacionEncuesta()
            .enlaceUnico(UPDATED_ENLACE_UNICO)
            .fechaAplicacion(UPDATED_FECHA_APLICACION)
            .respuestaEncuesta(UPDATED_RESPUESTA_ENCUESTA);
    }

    @BeforeEach
    void initTest() {
        aplicacionEncuesta = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAplicacionEncuesta != null) {
            aplicacionEncuestaRepository.delete(insertedAplicacionEncuesta);
            insertedAplicacionEncuesta = null;
        }
    }

    @Test
    @Transactional
    void createAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AplicacionEncuesta
        var returnedAplicacionEncuesta = om.readValue(
            restAplicacionEncuestaMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(aplicacionEncuesta))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AplicacionEncuesta.class
        );

        // Validate the AplicacionEncuesta in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertAplicacionEncuestaUpdatableFieldsEquals(
            returnedAplicacionEncuesta,
            getPersistedAplicacionEncuesta(returnedAplicacionEncuesta)
        );

        insertedAplicacionEncuesta = returnedAplicacionEncuesta;
    }

    @Test
    @Transactional
    void createAplicacionEncuestaWithExistingId() throws Exception {
        // Create the AplicacionEncuesta with an existing ID
        aplicacionEncuesta.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAplicacionEncuestaMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAplicacionEncuestas() throws Exception {
        // Initialize the database
        insertedAplicacionEncuesta = aplicacionEncuestaRepository.saveAndFlush(aplicacionEncuesta);

        // Get all the aplicacionEncuestaList
        restAplicacionEncuestaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(aplicacionEncuesta.getId().intValue())))
            .andExpect(jsonPath("$.[*].enlaceUnico").value(hasItem(DEFAULT_ENLACE_UNICO)))
            .andExpect(jsonPath("$.[*].fechaAplicacion").value(hasItem(DEFAULT_FECHA_APLICACION.toString())))
            .andExpect(jsonPath("$.[*].respuestaEncuesta").value(hasItem(DEFAULT_RESPUESTA_ENCUESTA)));
    }

    @Test
    @Transactional
    void getAplicacionEncuesta() throws Exception {
        // Initialize the database
        insertedAplicacionEncuesta = aplicacionEncuestaRepository.saveAndFlush(aplicacionEncuesta);

        // Get the aplicacionEncuesta
        restAplicacionEncuestaMockMvc
            .perform(get(ENTITY_API_URL_ID, aplicacionEncuesta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(aplicacionEncuesta.getId().intValue()))
            .andExpect(jsonPath("$.enlaceUnico").value(DEFAULT_ENLACE_UNICO))
            .andExpect(jsonPath("$.fechaAplicacion").value(DEFAULT_FECHA_APLICACION.toString()))
            .andExpect(jsonPath("$.respuestaEncuesta").value(DEFAULT_RESPUESTA_ENCUESTA));
    }

    @Test
    @Transactional
    void getNonExistingAplicacionEncuesta() throws Exception {
        // Get the aplicacionEncuesta
        restAplicacionEncuestaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAplicacionEncuesta() throws Exception {
        // Initialize the database
        insertedAplicacionEncuesta = aplicacionEncuestaRepository.saveAndFlush(aplicacionEncuesta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the aplicacionEncuesta
        AplicacionEncuesta updatedAplicacionEncuesta = aplicacionEncuestaRepository.findById(aplicacionEncuesta.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAplicacionEncuesta are not directly saved in db
        em.detach(updatedAplicacionEncuesta);
        updatedAplicacionEncuesta
            .enlaceUnico(UPDATED_ENLACE_UNICO)
            .fechaAplicacion(UPDATED_FECHA_APLICACION)
            .respuestaEncuesta(UPDATED_RESPUESTA_ENCUESTA);

        restAplicacionEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAplicacionEncuesta.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedAplicacionEncuesta))
            )
            .andExpect(status().isOk());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAplicacionEncuestaToMatchAllProperties(updatedAplicacionEncuesta);
    }

    @Test
    @Transactional
    void putNonExistingAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aplicacionEncuesta.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAplicacionEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, aplicacionEncuesta.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aplicacionEncuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAplicacionEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aplicacionEncuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAplicacionEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAplicacionEncuestaWithPatch() throws Exception {
        // Initialize the database
        insertedAplicacionEncuesta = aplicacionEncuestaRepository.saveAndFlush(aplicacionEncuesta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the aplicacionEncuesta using partial update
        AplicacionEncuesta partialUpdatedAplicacionEncuesta = new AplicacionEncuesta();
        partialUpdatedAplicacionEncuesta.setId(aplicacionEncuesta.getId());

        partialUpdatedAplicacionEncuesta.enlaceUnico(UPDATED_ENLACE_UNICO).fechaAplicacion(UPDATED_FECHA_APLICACION);

        restAplicacionEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAplicacionEncuesta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAplicacionEncuesta))
            )
            .andExpect(status().isOk());

        // Validate the AplicacionEncuesta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAplicacionEncuestaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAplicacionEncuesta, aplicacionEncuesta),
            getPersistedAplicacionEncuesta(aplicacionEncuesta)
        );
    }

    @Test
    @Transactional
    void fullUpdateAplicacionEncuestaWithPatch() throws Exception {
        // Initialize the database
        insertedAplicacionEncuesta = aplicacionEncuestaRepository.saveAndFlush(aplicacionEncuesta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the aplicacionEncuesta using partial update
        AplicacionEncuesta partialUpdatedAplicacionEncuesta = new AplicacionEncuesta();
        partialUpdatedAplicacionEncuesta.setId(aplicacionEncuesta.getId());

        partialUpdatedAplicacionEncuesta
            .enlaceUnico(UPDATED_ENLACE_UNICO)
            .fechaAplicacion(UPDATED_FECHA_APLICACION)
            .respuestaEncuesta(UPDATED_RESPUESTA_ENCUESTA);

        restAplicacionEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAplicacionEncuesta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAplicacionEncuesta))
            )
            .andExpect(status().isOk());

        // Validate the AplicacionEncuesta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAplicacionEncuestaUpdatableFieldsEquals(
            partialUpdatedAplicacionEncuesta,
            getPersistedAplicacionEncuesta(partialUpdatedAplicacionEncuesta)
        );
    }

    @Test
    @Transactional
    void patchNonExistingAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aplicacionEncuesta.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAplicacionEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, aplicacionEncuesta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aplicacionEncuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAplicacionEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAplicacionEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aplicacionEncuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAplicacionEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(aplicacionEncuesta))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AplicacionEncuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAplicacionEncuesta() throws Exception {
        // Initialize the database
        insertedAplicacionEncuesta = aplicacionEncuestaRepository.saveAndFlush(aplicacionEncuesta);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the aplicacionEncuesta
        restAplicacionEncuestaMockMvc
            .perform(delete(ENTITY_API_URL_ID, aplicacionEncuesta.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return aplicacionEncuestaRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected AplicacionEncuesta getPersistedAplicacionEncuesta(AplicacionEncuesta aplicacionEncuesta) {
        return aplicacionEncuestaRepository.findById(aplicacionEncuesta.getId()).orElseThrow();
    }

    protected void assertPersistedAplicacionEncuestaToMatchAllProperties(AplicacionEncuesta expectedAplicacionEncuesta) {
        assertAplicacionEncuestaAllPropertiesEquals(expectedAplicacionEncuesta, getPersistedAplicacionEncuesta(expectedAplicacionEncuesta));
    }

    protected void assertPersistedAplicacionEncuestaToMatchUpdatableProperties(AplicacionEncuesta expectedAplicacionEncuesta) {
        assertAplicacionEncuestaAllUpdatablePropertiesEquals(
            expectedAplicacionEncuesta,
            getPersistedAplicacionEncuesta(expectedAplicacionEncuesta)
        );
    }
}

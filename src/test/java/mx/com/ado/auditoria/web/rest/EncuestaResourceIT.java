package mx.com.ado.auditoria.web.rest;

import static mx.com.ado.auditoria.domain.EncuestaAsserts.*;
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
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.repository.EncuestaRepository;
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
 * Integration tests for the {@link EncuestaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EncuestaResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_VIGENCIA_DESDE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VIGENCIA_DESDE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_VIGENCIA_HASTA = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VIGENCIA_HASTA = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_STRUCTURA_JSON = "AAAAAAAAAA";
    private static final String UPDATED_STRUCTURA_JSON = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/encuestas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EncuestaRepository encuestaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEncuestaMockMvc;

    private Encuesta encuesta;

    private Encuesta insertedEncuesta;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Encuesta createEntity() {
        return new Encuesta()
            .nombre(DEFAULT_NOMBRE)
            .vigenciaDesde(DEFAULT_VIGENCIA_DESDE)
            .vigenciaHasta(DEFAULT_VIGENCIA_HASTA)
            .structuraJson(DEFAULT_STRUCTURA_JSON);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Encuesta createUpdatedEntity() {
        return new Encuesta()
            .nombre(UPDATED_NOMBRE)
            .vigenciaDesde(UPDATED_VIGENCIA_DESDE)
            .vigenciaHasta(UPDATED_VIGENCIA_HASTA)
            .structuraJson(UPDATED_STRUCTURA_JSON);
    }

    @BeforeEach
    void initTest() {
        encuesta = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEncuesta != null) {
            encuestaRepository.delete(insertedEncuesta);
            insertedEncuesta = null;
        }
    }

    @Test
    @Transactional
    void createEncuesta() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Encuesta
        var returnedEncuesta = om.readValue(
            restEncuestaMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(encuesta)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Encuesta.class
        );

        // Validate the Encuesta in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertEncuestaUpdatableFieldsEquals(returnedEncuesta, getPersistedEncuesta(returnedEncuesta));

        insertedEncuesta = returnedEncuesta;
    }

    @Test
    @Transactional
    void createEncuestaWithExistingId() throws Exception {
        // Create the Encuesta with an existing ID
        encuesta.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEncuestaMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(encuesta)))
            .andExpect(status().isBadRequest());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllEncuestas() throws Exception {
        // Initialize the database
        insertedEncuesta = encuestaRepository.saveAndFlush(encuesta);

        // Get all the encuestaList
        restEncuestaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(encuesta.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].vigenciaDesde").value(hasItem(DEFAULT_VIGENCIA_DESDE.toString())))
            .andExpect(jsonPath("$.[*].vigenciaHasta").value(hasItem(DEFAULT_VIGENCIA_HASTA.toString())))
            .andExpect(jsonPath("$.[*].structuraJson").value(hasItem(DEFAULT_STRUCTURA_JSON)));
    }

    @Test
    @Transactional
    void getEncuesta() throws Exception {
        // Initialize the database
        insertedEncuesta = encuestaRepository.saveAndFlush(encuesta);

        // Get the encuesta
        restEncuestaMockMvc
            .perform(get(ENTITY_API_URL_ID, encuesta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(encuesta.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.vigenciaDesde").value(DEFAULT_VIGENCIA_DESDE.toString()))
            .andExpect(jsonPath("$.vigenciaHasta").value(DEFAULT_VIGENCIA_HASTA.toString()))
            .andExpect(jsonPath("$.structuraJson").value(DEFAULT_STRUCTURA_JSON));
    }

    @Test
    @Transactional
    void getNonExistingEncuesta() throws Exception {
        // Get the encuesta
        restEncuestaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEncuesta() throws Exception {
        // Initialize the database
        insertedEncuesta = encuestaRepository.saveAndFlush(encuesta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the encuesta
        Encuesta updatedEncuesta = encuestaRepository.findById(encuesta.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEncuesta are not directly saved in db
        em.detach(updatedEncuesta);
        updatedEncuesta
            .nombre(UPDATED_NOMBRE)
            .vigenciaDesde(UPDATED_VIGENCIA_DESDE)
            .vigenciaHasta(UPDATED_VIGENCIA_HASTA)
            .structuraJson(UPDATED_STRUCTURA_JSON);

        restEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedEncuesta.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedEncuesta))
            )
            .andExpect(status().isOk());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEncuestaToMatchAllProperties(updatedEncuesta);
    }

    @Test
    @Transactional
    void putNonExistingEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuesta.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, encuesta.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(encuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(encuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestaMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(encuesta)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEncuestaWithPatch() throws Exception {
        // Initialize the database
        insertedEncuesta = encuestaRepository.saveAndFlush(encuesta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the encuesta using partial update
        Encuesta partialUpdatedEncuesta = new Encuesta();
        partialUpdatedEncuesta.setId(encuesta.getId());

        partialUpdatedEncuesta.nombre(UPDATED_NOMBRE).vigenciaDesde(UPDATED_VIGENCIA_DESDE).vigenciaHasta(UPDATED_VIGENCIA_HASTA);

        restEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEncuesta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEncuesta))
            )
            .andExpect(status().isOk());

        // Validate the Encuesta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEncuestaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedEncuesta, encuesta), getPersistedEncuesta(encuesta));
    }

    @Test
    @Transactional
    void fullUpdateEncuestaWithPatch() throws Exception {
        // Initialize the database
        insertedEncuesta = encuestaRepository.saveAndFlush(encuesta);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the encuesta using partial update
        Encuesta partialUpdatedEncuesta = new Encuesta();
        partialUpdatedEncuesta.setId(encuesta.getId());

        partialUpdatedEncuesta
            .nombre(UPDATED_NOMBRE)
            .vigenciaDesde(UPDATED_VIGENCIA_DESDE)
            .vigenciaHasta(UPDATED_VIGENCIA_HASTA)
            .structuraJson(UPDATED_STRUCTURA_JSON);

        restEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEncuesta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEncuesta))
            )
            .andExpect(status().isOk());

        // Validate the Encuesta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEncuestaUpdatableFieldsEquals(partialUpdatedEncuesta, getPersistedEncuesta(partialUpdatedEncuesta));
    }

    @Test
    @Transactional
    void patchNonExistingEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuesta.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, encuesta.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(encuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(encuesta))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEncuesta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuesta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestaMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(encuesta)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Encuesta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEncuesta() throws Exception {
        // Initialize the database
        insertedEncuesta = encuestaRepository.saveAndFlush(encuesta);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the encuesta
        restEncuestaMockMvc
            .perform(delete(ENTITY_API_URL_ID, encuesta.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return encuestaRepository.count();
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

    protected Encuesta getPersistedEncuesta(Encuesta encuesta) {
        return encuestaRepository.findById(encuesta.getId()).orElseThrow();
    }

    protected void assertPersistedEncuestaToMatchAllProperties(Encuesta expectedEncuesta) {
        assertEncuestaAllPropertiesEquals(expectedEncuesta, getPersistedEncuesta(expectedEncuesta));
    }

    protected void assertPersistedEncuestaToMatchUpdatableProperties(Encuesta expectedEncuesta) {
        assertEncuestaAllUpdatablePropertiesEquals(expectedEncuesta, getPersistedEncuesta(expectedEncuesta));
    }
}

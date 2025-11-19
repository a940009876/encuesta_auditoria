package mx.com.ado.auditoria.web.rest;

import static mx.com.ado.auditoria.domain.EncuestadoAsserts.*;
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
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.repository.EncuestadoRepository;
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
 * Integration tests for the {@link EncuestadoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EncuestadoResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_FECHA_NACIMIENTO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FECHA_NACIMIENTO = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_CLAVE_EMPLEADO = "AAAAAAAAAA";
    private static final String UPDATED_CLAVE_EMPLEADO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/encuestados";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EncuestadoRepository encuestadoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEncuestadoMockMvc;

    private Encuestado encuestado;

    private Encuestado insertedEncuestado;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Encuestado createEntity() {
        return new Encuestado().nombre(DEFAULT_NOMBRE).fechaNacimiento(DEFAULT_FECHA_NACIMIENTO).claveEmpleado(DEFAULT_CLAVE_EMPLEADO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Encuestado createUpdatedEntity() {
        return new Encuestado().nombre(UPDATED_NOMBRE).fechaNacimiento(UPDATED_FECHA_NACIMIENTO).claveEmpleado(UPDATED_CLAVE_EMPLEADO);
    }

    @BeforeEach
    void initTest() {
        encuestado = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEncuestado != null) {
            encuestadoRepository.delete(insertedEncuestado);
            insertedEncuestado = null;
        }
    }

    @Test
    @Transactional
    void createEncuestado() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Encuestado
        var returnedEncuestado = om.readValue(
            restEncuestadoMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(encuestado))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Encuestado.class
        );

        // Validate the Encuestado in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertEncuestadoUpdatableFieldsEquals(returnedEncuestado, getPersistedEncuestado(returnedEncuestado));

        insertedEncuestado = returnedEncuestado;
    }

    @Test
    @Transactional
    void createEncuestadoWithExistingId() throws Exception {
        // Create the Encuestado with an existing ID
        encuestado.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEncuestadoMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(encuestado)))
            .andExpect(status().isBadRequest());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllEncuestados() throws Exception {
        // Initialize the database
        insertedEncuestado = encuestadoRepository.saveAndFlush(encuestado);

        // Get all the encuestadoList
        restEncuestadoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(encuestado.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].fechaNacimiento").value(hasItem(DEFAULT_FECHA_NACIMIENTO.toString())))
            .andExpect(jsonPath("$.[*].claveEmpleado").value(hasItem(DEFAULT_CLAVE_EMPLEADO)));
    }

    @Test
    @Transactional
    void getEncuestado() throws Exception {
        // Initialize the database
        insertedEncuestado = encuestadoRepository.saveAndFlush(encuestado);

        // Get the encuestado
        restEncuestadoMockMvc
            .perform(get(ENTITY_API_URL_ID, encuestado.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(encuestado.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.fechaNacimiento").value(DEFAULT_FECHA_NACIMIENTO.toString()))
            .andExpect(jsonPath("$.claveEmpleado").value(DEFAULT_CLAVE_EMPLEADO));
    }

    @Test
    @Transactional
    void getNonExistingEncuestado() throws Exception {
        // Get the encuestado
        restEncuestadoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEncuestado() throws Exception {
        // Initialize the database
        insertedEncuestado = encuestadoRepository.saveAndFlush(encuestado);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the encuestado
        Encuestado updatedEncuestado = encuestadoRepository.findById(encuestado.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEncuestado are not directly saved in db
        em.detach(updatedEncuestado);
        updatedEncuestado.nombre(UPDATED_NOMBRE).fechaNacimiento(UPDATED_FECHA_NACIMIENTO).claveEmpleado(UPDATED_CLAVE_EMPLEADO);

        restEncuestadoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedEncuestado.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedEncuestado))
            )
            .andExpect(status().isOk());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEncuestadoToMatchAllProperties(updatedEncuestado);
    }

    @Test
    @Transactional
    void putNonExistingEncuestado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuestado.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEncuestadoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, encuestado.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(encuestado))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEncuestado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuestado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestadoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(encuestado))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEncuestado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuestado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestadoMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(encuestado)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEncuestadoWithPatch() throws Exception {
        // Initialize the database
        insertedEncuestado = encuestadoRepository.saveAndFlush(encuestado);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the encuestado using partial update
        Encuestado partialUpdatedEncuestado = new Encuestado();
        partialUpdatedEncuestado.setId(encuestado.getId());

        partialUpdatedEncuestado.claveEmpleado(UPDATED_CLAVE_EMPLEADO);

        restEncuestadoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEncuestado.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEncuestado))
            )
            .andExpect(status().isOk());

        // Validate the Encuestado in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEncuestadoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEncuestado, encuestado),
            getPersistedEncuestado(encuestado)
        );
    }

    @Test
    @Transactional
    void fullUpdateEncuestadoWithPatch() throws Exception {
        // Initialize the database
        insertedEncuestado = encuestadoRepository.saveAndFlush(encuestado);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the encuestado using partial update
        Encuestado partialUpdatedEncuestado = new Encuestado();
        partialUpdatedEncuestado.setId(encuestado.getId());

        partialUpdatedEncuestado.nombre(UPDATED_NOMBRE).fechaNacimiento(UPDATED_FECHA_NACIMIENTO).claveEmpleado(UPDATED_CLAVE_EMPLEADO);

        restEncuestadoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEncuestado.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEncuestado))
            )
            .andExpect(status().isOk());

        // Validate the Encuestado in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEncuestadoUpdatableFieldsEquals(partialUpdatedEncuestado, getPersistedEncuestado(partialUpdatedEncuestado));
    }

    @Test
    @Transactional
    void patchNonExistingEncuestado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuestado.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEncuestadoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, encuestado.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(encuestado))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEncuestado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuestado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestadoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(encuestado))
            )
            .andExpect(status().isBadRequest());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEncuestado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        encuestado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEncuestadoMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(encuestado))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Encuestado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEncuestado() throws Exception {
        // Initialize the database
        insertedEncuestado = encuestadoRepository.saveAndFlush(encuestado);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the encuestado
        restEncuestadoMockMvc
            .perform(delete(ENTITY_API_URL_ID, encuestado.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return encuestadoRepository.count();
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

    protected Encuestado getPersistedEncuestado(Encuestado encuestado) {
        return encuestadoRepository.findById(encuestado.getId()).orElseThrow();
    }

    protected void assertPersistedEncuestadoToMatchAllProperties(Encuestado expectedEncuestado) {
        assertEncuestadoAllPropertiesEquals(expectedEncuestado, getPersistedEncuestado(expectedEncuestado));
    }

    protected void assertPersistedEncuestadoToMatchUpdatableProperties(Encuestado expectedEncuestado) {
        assertEncuestadoAllUpdatablePropertiesEquals(expectedEncuestado, getPersistedEncuestado(expectedEncuestado));
    }
}

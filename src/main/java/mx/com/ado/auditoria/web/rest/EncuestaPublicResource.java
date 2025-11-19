package mx.com.ado.auditoria.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.service.AplicacionEncuestaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for public survey access.
 */
@RestController
@RequestMapping("/api/encuesta")
public class EncuestaPublicResource {

    private static final Logger LOG = LoggerFactory.getLogger(EncuestaPublicResource.class);

    private final AplicacionEncuestaService aplicacionEncuestaService;
    private final ObjectMapper objectMapper;

    public EncuestaPublicResource(AplicacionEncuestaService aplicacionEncuestaService, ObjectMapper objectMapper) {
        this.aplicacionEncuestaService = aplicacionEncuestaService;
        this.objectMapper = objectMapper;
    }

    /**
     * {@code GET  /encuesta/:enlaceUnico} : Get survey data by enlaceUnico.
     *
     * @param enlaceUnico the enlaceUnico of the aplicacionEncuesta to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the survey data, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{enlaceUnico}")
    public ResponseEntity<Map<String, Object>> getEncuestaByEnlace(@PathVariable("enlaceUnico") String enlaceUnico) {
        LOG.info("=== INICIO: Cargar encuesta por enlaceUnico ===");
        LOG.info("enlaceUnico recibido: {}", enlaceUnico);

        try {
            LOG.info("Buscando AplicacionEncuesta con enlaceUnico: {}", enlaceUnico);
            Optional<AplicacionEncuesta> aplicacionEncuestaOpt = aplicacionEncuestaService.findByEnlaceUnico(enlaceUnico);

            if (aplicacionEncuestaOpt.isEmpty()) {
                LOG.warn("No se encontró AplicacionEncuesta con enlaceUnico: {}", enlaceUnico);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            AplicacionEncuesta aplicacionEncuesta = aplicacionEncuestaOpt.get();
            LOG.info("AplicacionEncuesta encontrada - ID: {}, fechaAplicacion: {}", 
                aplicacionEncuesta.getId(), aplicacionEncuesta.getFechaAplicacion());

            // Verificar que fechaAplicacion sea null
            if (aplicacionEncuesta.getFechaAplicacion() != null) {
                LOG.warn("La encuesta con ID {} ya ha sido completada. fechaAplicacion: {}", 
                    aplicacionEncuesta.getId(), aplicacionEncuesta.getFechaAplicacion());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Esta encuesta ya ha sido completada"));
            }

            Encuesta encuesta = aplicacionEncuesta.getEncuesta();
            Encuestado encuestado = aplicacionEncuesta.getEncuestado();

            LOG.info("Verificando encuesta y encuestado - encuesta: {}, encuestado: {}", 
                encuesta != null ? "presente (ID: " + encuesta.getId() + ")" : "null",
                encuestado != null ? "presente (ID: " + encuestado.getId() + ")" : "null");

            if (encuesta == null || encuestado == null) {
                LOG.error("Datos incompletos - encuesta es null: {}, encuestado es null: {}", 
                    encuesta == null, encuestado == null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            LOG.info("Obteniendo structuraJson de la encuesta ID: {}", encuesta.getId());
            String structuraJson = encuesta.getStructuraJson();
            
            if (structuraJson == null || structuraJson.trim().isEmpty()) {
                LOG.error("La encuesta ID {} no tiene structuraJson o está vacío", encuesta.getId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "La encuesta no tiene estructura definida"));
            }

            LOG.info("Parseando structuraJson (longitud: {} caracteres)", structuraJson.length());
            try {
                // Parsear el JSON de la estructura
                @SuppressWarnings("unchecked")
                Map<String, Object> estructuraJson = (Map<String, Object>) objectMapper.readValue(structuraJson, Map.class);
                LOG.info("structuraJson parseado exitosamente");

                // Construir la respuesta
                LOG.info("Construyendo respuesta con aplicacionEncuestaId: {}", aplicacionEncuesta.getId());
                Map<String, Object> response = new HashMap<>();
                response.put("aplicacionEncuestaId", aplicacionEncuesta.getId());
                response.put("encuestado", Map.of(
                    "id", encuestado.getId(),
                    "nombre", encuestado.getNombre() != null ? encuestado.getNombre() : "",
                    "fechaNacimiento", encuestado.getFechaNacimiento() != null ? encuestado.getFechaNacimiento().toString() : "",
                    "claveEmpleado", encuestado.getClaveEmpleado() != null ? encuestado.getClaveEmpleado() : ""
                ));
                response.put("encuesta", estructuraJson);

                LOG.info("=== ÉXITO: Encuesta cargada correctamente ===");
                return ResponseEntity.ok(response);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                LOG.error("Error al parsear structuraJson de la encuesta ID: {}", encuesta.getId(), e);
                LOG.error("Contenido de structuraJson (primeros 500 caracteres): {}", 
                    structuraJson.length() > 500 ? structuraJson.substring(0, 500) + "..." : structuraJson);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la estructura de la encuesta: " + e.getMessage()));
            }
        } catch (Exception e) {
            LOG.error("=== ERROR INESPERADO al cargar encuesta ===", e);
            LOG.error("Tipo de excepción: {}", e.getClass().getName());
            LOG.error("Mensaje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error inesperado al cargar la encuesta: " + e.getMessage()));
        }
    }

    /**
     * {@code PUT  /encuesta/:enlaceUnico/finalizar} : Finalize survey with answers.
     *
     * @param enlaceUnico the enlaceUnico of the aplicacionEncuesta.
     * @param respuestas the answers JSON.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} or with status {@code 404 (Not Found)}.
     */
    @PutMapping("/{enlaceUnico}/finalizar")
    public ResponseEntity<Map<String, Object>> finalizarEncuesta(
        @PathVariable("enlaceUnico") String enlaceUnico,
        @RequestBody Map<String, Object> respuestas
    ) {
        LOG.debug("REST request to finalize Encuesta by enlaceUnico : {}", enlaceUnico);

        Optional<AplicacionEncuesta> aplicacionEncuestaOpt = aplicacionEncuestaService.findByEnlaceUnico(enlaceUnico);

        if (aplicacionEncuestaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        AplicacionEncuesta aplicacionEncuesta = aplicacionEncuestaOpt.get();

        // Verificar que fechaAplicacion sea null
        if (aplicacionEncuesta.getFechaAplicacion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Esta encuesta ya ha sido completada"));
        }

        try {
            // Convertir respuestas a JSON string
            String respuestaEncuestaJson = objectMapper.writeValueAsString(respuestas);

            // Actualizar la aplicacionEncuesta
            aplicacionEncuesta.setRespuestaEncuesta(respuestaEncuestaJson);
            aplicacionEncuesta.setFechaAplicacion(LocalDate.now());

            aplicacionEncuestaService.update(aplicacionEncuesta);

            return ResponseEntity.ok(Map.of("message", "Encuesta finalizada exitosamente"));
        } catch (Exception e) {
            LOG.error("Error finalizing survey", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al finalizar la encuesta"));
        }
    }
}


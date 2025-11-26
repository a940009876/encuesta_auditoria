package mx.com.ado.auditoria.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringWriter;
import java.util.*;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.security.AuthoritiesConstants;
import mx.com.ado.auditoria.service.AplicacionEncuestaService;
import mx.com.ado.auditoria.service.EncuestaService;
import mx.com.ado.auditoria.service.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for generating enlaces for encuestas.
 */
@RestController
@RequestMapping("/api/admin")
public class GenerarEnlacesResource {

    private static final Logger LOG = LoggerFactory.getLogger(GenerarEnlacesResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EncuestaService encuestaService;
    private final AplicacionEncuestaService aplicacionEncuestaService;
    private final ObjectMapper objectMapper;
    private final EncryptionService encryptionService;

    public GenerarEnlacesResource(
        EncuestaService encuestaService,
        AplicacionEncuestaService aplicacionEncuestaService,
        ObjectMapper objectMapper,
        EncryptionService encryptionService
    ) {
        this.encuestaService = encuestaService;
        this.aplicacionEncuestaService = aplicacionEncuestaService;
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
    }

    /**
     * {@code GET  /admin/encuestas-vigentes} : get all vigentes encuestas.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of vigentes encuestas in body.
     */
    @GetMapping("/encuestas-vigentes")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Encuesta>> getEncuestasVigentes() {
        LOG.debug("REST request to get all vigentes Encuestas");
        List<Encuesta> encuestas = encuestaService.findVigentesOrderByIdDesc();
        return ResponseEntity.ok().body(encuestas);
    }

    /**
     * {@code POST  /admin/generar-enlaces/:encuestaId} : Generate enlaces for all encuestados with the given encuesta.
     *
     * @param encuestaId the id of the encuesta.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the number of enlaces created in body.
     */
    @PostMapping("/generar-enlaces/{encuestaId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Integer> generarEnlaces(@PathVariable Long encuestaId) {
        LOG.debug("REST request to generate enlaces for encuesta : {}", encuestaId);
        int count = aplicacionEncuestaService.generarEnlacesParaEncuestados(encuestaId);
        return ResponseEntity.ok().body(count);
    }

    /**
     * {@code GET  /admin/exportar-encuesta/:encuestaId} : Export encuesta responses to CSV.
     *
     * @param encuestaId the id of the encuesta.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and CSV file in body.
     */
    @GetMapping("/exportar-encuesta/{encuestaId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<byte[]> exportarEncuesta(@PathVariable Long encuestaId) {
        LOG.debug("REST request to export encuesta : {}", encuestaId);

        Optional<Encuesta> encuestaOpt = encuestaService.findOne(encuestaId);
        if (encuestaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Encuesta encuesta = encuestaOpt.get();
        List<AplicacionEncuesta> aplicaciones = aplicacionEncuestaService.findCompletadasByEncuestaId(encuestaId);

        try {
            // Parse survey structure
            String structuraJson = encuesta.getStructuraJson();
            if (structuraJson == null || structuraJson.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La encuesta no tiene estructura definida".getBytes());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> estructura = (Map<String, Object>) objectMapper.readValue(structuraJson, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> preguntas = (List<Map<String, Object>>) estructura.get("preguntas");

            if (preguntas == null) {
                return ResponseEntity.badRequest().body("La estructura de la encuesta no es válida".getBytes());
            }

            // Generate CSV
            StringWriter csvWriter = new StringWriter();

            // Write header
            csvWriter.append("nombre_encuestado,clave_empleado");
            for (int i = 0; i < preguntas.size(); i++) {
                int preguntaNum = i + 1;
                csvWriter.append(",pregunta").append(String.valueOf(preguntaNum));
                csvWriter.append(",respuesta").append(String.valueOf(preguntaNum));
                csvWriter.append(",texto").append(String.valueOf(preguntaNum));
            }
            csvWriter.append("\n");

            // Write data rows
            for (AplicacionEncuesta aplicacion : aplicaciones) {
                Encuestado encuestado = aplicacion.getEncuestado();
                String nombreEncuestado = encuestado != null && encuestado.getNombre() != null
                    ? escapeCsv(encuestado.getNombre())
                    : "";
                String claveEmpleado = encuestado != null && encuestado.getClaveEmpleado() != null
                    ? escapeCsv(encuestado.getClaveEmpleado())
                    : "";

                csvWriter.append(nombreEncuestado).append(",").append(claveEmpleado);

                // Parse responses
                String respuestaJsonEncriptada = aplicacion.getRespuestaEncuesta();
                Map<Integer, Map<String, Object>> respuestasMap = new HashMap<>();
                if (respuestaJsonEncriptada != null && !respuestaJsonEncriptada.trim().isEmpty()) {
                    try {
                        // Desencriptar las respuestas antes de parsearlas
                        String respuestaJson = encryptionService.decrypt(respuestaJsonEncriptada);
                        LOG.debug("Respuestas desencriptadas exitosamente para aplicacionEncuesta ID: {}", aplicacion.getId());

                        @SuppressWarnings("unchecked")
                        Map<String, Object> respuestasData = (Map<String, Object>) objectMapper.readValue(respuestaJson, Map.class);
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> respuestas = (List<Map<String, Object>>) respuestasData.get("respuestas");
                        if (respuestas != null) {
                            for (Map<String, Object> respuesta : respuestas) {
                                Integer preguntaIndice = ((Number) respuesta.get("pregunta")).intValue();
                                respuestasMap.put(preguntaIndice, respuesta);
                            }
                        }
                    } catch (Exception e) {
                        LOG.warn("Error parsing or decrypting respuesta JSON for aplicacion {}: {}", aplicacion.getId(), e.getMessage());
                    }
                }

                // Write responses for each question
                for (int i = 0; i < preguntas.size(); i++) {
                    Map<String, Object> pregunta = preguntas.get(i);
                    Integer preguntaIndice = ((Number) pregunta.get("indice")).intValue();
                    String preguntaEnunciado = pregunta.get("enunciado") != null
                        ? escapeCsv(pregunta.get("enunciado").toString())
                        : "";

                    Map<String, Object> respuesta = respuestasMap.get(preguntaIndice);
                    String respuestaTexto = "";
                    String textoAdicional = "";

                    if (respuesta != null) {
                        Integer respuestaIndice = respuesta.get("respuesta") != null
                            ? ((Number) respuesta.get("respuesta")).intValue()
                            : null;

                        if (respuestaIndice != null) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> opciones = (List<Map<String, Object>>) pregunta.get("opciones");
                            if (opciones != null) {
                                for (Map<String, Object> opcion : opciones) {
                                    Integer opcionIndice = ((Number) opcion.get("indice")).intValue();
                                    if (opcionIndice.equals(respuestaIndice)) {
                                        respuestaTexto = escapeCsv(opcion.get("enunciado").toString());
                                        break;
                                    }
                                }
                            }
                        }

                        if (respuesta.get("respuesta_texto") != null) {
                            textoAdicional = escapeCsv(respuesta.get("respuesta_texto").toString());
                        }
                    }

                    csvWriter.append(",").append(preguntaEnunciado);
                    csvWriter.append(",").append(respuestaTexto);
                    csvWriter.append(",").append(textoAdicional);
                }

                csvWriter.append("\n");
            }

            byte[] csvBytes = csvWriter.toString().getBytes("UTF-8");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", encuesta.getNombre().replaceAll("\\s+", "_").toLowerCase() + ".csv");

            return ResponseEntity.ok().headers(headers).body(csvBytes);
        } catch (Exception e) {
            LOG.error("Error exporting encuesta", e);
            return ResponseEntity.internalServerError().body(("Error al exportar la encuesta: " + e.getMessage()).getBytes());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}


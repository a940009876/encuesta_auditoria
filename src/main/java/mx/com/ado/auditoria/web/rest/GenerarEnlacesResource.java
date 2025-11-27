package mx.com.ado.auditoria.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.repository.EncuestadoRepository;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for generating enlaces for encuestas and importing encuestados.
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
    private final EncuestadoRepository encuestadoRepository;

    private static final DateTimeFormatter FECHA_NACIMIENTO_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GenerarEnlacesResource(
        EncuestaService encuestaService,
        AplicacionEncuestaService aplicacionEncuestaService,
        ObjectMapper objectMapper,
        EncryptionService encryptionService,
        EncuestadoRepository encuestadoRepository
    ) {
        this.encuestaService = encuestaService;
        this.aplicacionEncuestaService = aplicacionEncuestaService;
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
        this.encuestadoRepository = encuestadoRepository;
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

    /**
     * {@code POST /admin/importar-encuestados} : Import encuestados from CSV file.
     *
     * CSV structure: Nombre,Clave,Fecha de Nacimiento (dd/MM/yyyy)
     *
     * Validation rules:
     * 1. Each record must contain the 3 fields.
     * 2. Employee key (Clave) must be unique.
     * 3. Birth date format must be dd/MM/yyyy.
     */
    @PostMapping(value = "/importar-encuestados", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ImportEncuestadosResult> importarEncuestados(@RequestPart("file") MultipartFile file) {
        LOG.debug("REST request to import encuestados from CSV");

        ImportEncuestadosResult result = new ImportEncuestadosResult();

        if (file == null || file.isEmpty()) {
            result.getRegistrosConProblemas()
                .add(new RegistroConProblema("", "El archivo está vacío o no fue enviado"));
            return ResponseEntity.badRequest().body(result);
        }

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Skip header if present
                if (isFirstLine) {
                    String lower = line.toLowerCase();
                    if (lower.contains("nombre") && lower.contains("clave")) {
                        isFirstLine = false;
                        continue;
                    }
                    isFirstLine = false;
                }

                String originalLine = line;
                String[] parts = line.split(",");

                if (parts.length != 3) {
                    result.getRegistrosConProblemas()
                        .add(new RegistroConProblema(originalLine, "Cada registro debe contar con los 3 campos"));
                    continue;
                }

                String nombre = parts[0] != null ? parts[0].trim() : "";
                String clave = parts[1] != null ? parts[1].trim() : "";
                String fechaStr = parts[2] != null ? parts[2].trim() : "";

                if (nombre.isEmpty() || clave.isEmpty() || fechaStr.isEmpty()) {
                    result.getRegistrosConProblemas()
                        .add(new RegistroConProblema(originalLine, "Cada registro debe contar con los 3 campos"));
                    continue;
                }

                // Validate unique clave
                if (encuestadoRepository.existsByClaveEmpleado(clave)) {
                    result.getRegistrosConProblemas()
                        .add(new RegistroConProblema(originalLine, "La clave de empleado ya existe"));
                    continue;
                }

                // Validate date format
                LocalDate fechaNacimiento;
                try {
                    fechaNacimiento = LocalDate.parse(fechaStr, FECHA_NACIMIENTO_FORMATTER);
                } catch (DateTimeParseException e) {
                    result.getRegistrosConProblemas()
                        .add(new RegistroConProblema(originalLine, "El formato de la fecha de nacimiento es inválido"));
                    continue;
                }

                // Persist encuestado
                Encuestado encuestado = new Encuestado()
                    .nombre(nombre)
                    .claveEmpleado(clave)
                    .fechaNacimiento(fechaNacimiento);

                encuestadoRepository.save(encuestado);
                result.incrementRegistrosImportados();
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LOG.error("Error importing encuestados from CSV", e);
            result.getRegistrosConProblemas()
                .add(new RegistroConProblema("", "Error al procesar el archivo: " + e.getMessage()));
            return ResponseEntity.internalServerError().body(result);
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

    public static class ImportEncuestadosResult {

        private int registrosImportados;
        private java.util.List<RegistroConProblema> registrosConProblemas = new java.util.ArrayList<>();

        public int getRegistrosImportados() {
            return registrosImportados;
        }

        public void setRegistrosImportados(int registrosImportados) {
            this.registrosImportados = registrosImportados;
        }

        public void incrementRegistrosImportados() {
            this.registrosImportados++;
        }

        public java.util.List<RegistroConProblema> getRegistrosConProblemas() {
            return registrosConProblemas;
        }

        public void setRegistrosConProblemas(java.util.List<RegistroConProblema> registrosConProblemas) {
            this.registrosConProblemas = registrosConProblemas;
        }
    }

    public static class RegistroConProblema {

        private String lineaOriginal;
        private String motivo;

        public RegistroConProblema() {}

        public RegistroConProblema(String lineaOriginal, String motivo) {
            this.lineaOriginal = lineaOriginal;
            this.motivo = motivo;
        }

        public String getLineaOriginal() {
            return lineaOriginal;
        }

        public void setLineaOriginal(String lineaOriginal) {
            this.lineaOriginal = lineaOriginal;
        }

        public String getMotivo() {
            return motivo;
        }

        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
    }
}


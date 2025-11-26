package mx.com.ado.auditoria.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.*;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.repository.EncuestadoRepository;
import mx.com.ado.auditoria.service.AplicacionEncuestaService;
import mx.com.ado.auditoria.service.EncuestaService;
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
    private final EncuestaService encuestaService;
    private final EncuestadoRepository encuestadoRepository;

    public EncuestaPublicResource(
        AplicacionEncuestaService aplicacionEncuestaService,
        ObjectMapper objectMapper,
        EncuestaService encuestaService,
        EncuestadoRepository encuestadoRepository
    ) {
        this.aplicacionEncuestaService = aplicacionEncuestaService;
        this.objectMapper = objectMapper;
        this.encuestaService = encuestaService;
        this.encuestadoRepository = encuestadoRepository;
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
     * {@code GET  /encuesta/init/encuestas-vigentes} : get all vigentes encuestas ordered from oldest to newest.
     *
     * This endpoint is public and is used by the encuesta init flow.
     */
    @GetMapping("/init/encuestas-vigentes")
    public ResponseEntity<List<Encuesta>> getEncuestasVigentesParaInit() {
        LOG.debug("REST request to get vigentes Encuestas for init");
        List<Encuesta> encuestas = encuestaService.findVigentesOrderByIdDesc();
        // El servicio las regresa de más reciente a más antigua, invertimos para dejar de más antigua a más reciente
        Collections.reverse(encuestas);
        return ResponseEntity.ok(encuestas);
    }

    /**
     * Request body for the first step of encuesta init (buscar encuestado).
     */
    public static class EncuestaInitBuscarRequest {
        private Long encuestaId;
        private String nombre;
        private String claveEmpleado;

        public Long getEncuestaId() {
            return encuestaId;
        }

        public void setEncuestaId(Long encuestaId) {
            this.encuestaId = encuestaId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getClaveEmpleado() {
            return claveEmpleado;
        }

        public void setClaveEmpleado(String claveEmpleado) {
            this.claveEmpleado = claveEmpleado;
        }

    }

    /**
     * Response body for the first step of encuesta init.
     */
    public static class EncuestaInitBuscarResponse {
        private Long encuestaId;
        private Long encuestadoId;
        private String campoValidacion; // DIA, MES o ANIO

        public Long getEncuestaId() {
            return encuestaId;
        }

        public void setEncuestaId(Long encuestaId) {
            this.encuestaId = encuestaId;
        }

        public Long getEncuestadoId() {
            return encuestadoId;
        }

        public void setEncuestadoId(Long encuestadoId) {
            this.encuestadoId = encuestadoId;
        }

        public String getCampoValidacion() {
            return campoValidacion;
        }

        public void setCampoValidacion(String campoValidacion) {
            this.campoValidacion = campoValidacion;
        }
    }

    /**
     * {@code POST  /encuesta/init/buscar-encuestado} :
     * Busca al encuestado por nombre y clave de empleado para la encuesta seleccionada
     * y devuelve el campo de fecha de nacimiento que deberá capturar (día/mes/año).
     */
    @PostMapping("/init/buscar-encuestado")
    public ResponseEntity<?> buscarEncuestadoParaInit(@RequestBody EncuestaInitBuscarRequest request) {
        LOG.debug("REST request to buscar encuestado for init, encuestaId: {}, nombre: {}, claveEmpleado: {}", request.getEncuestaId(), request.getNombre(), request.getClaveEmpleado());

        if (request.getEncuestaId() == null || request.getNombre() == null || request.getClaveEmpleado() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Los datos de encuesta, nombre y clave de empleado son obligatorios"));
        }

        Optional<Encuestado> encuestadoOpt = encuestadoRepository.findByNombreAndClaveEmpleado(
            request.getNombre(),
            request.getClaveEmpleado()
        );

        if (encuestadoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No se encontró un encuestado con el nombre y la clave de empleado proporcionados"));
        }

        Encuestado encuestado = encuestadoOpt.get();

        if (encuestado.getFechaNacimiento() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "El encuestado no tiene registrada una fecha de nacimiento"));
        }

        // Seleccionar aleatoriamente qué parte de la fecha se va a solicitar como candado
        String[] campos = new String[] { "DIA", "MES", "ANIO" };
        String campoSeleccionado = campos[new Random().nextInt(campos.length)];

        EncuestaInitBuscarResponse response = new EncuestaInitBuscarResponse();
        response.setEncuestaId(request.getEncuestaId());
        response.setEncuestadoId(encuestado.getId());
        response.setCampoValidacion(campoSeleccionado);

        return ResponseEntity.ok(response);
    }

    /**
     * Request body for the validation step of encuesta init.
     */
    public static class EncuestaInitValidarRequest {
        private Long encuestaId;
        private Long encuestadoId;
        private String campoValidacion; // DIA, MES o ANIO
        private String valor; // siempre 2 dígitos

        public Long getEncuestaId() {
            return encuestaId;
        }

        public void setEncuestaId(Long encuestaId) {
            this.encuestaId = encuestaId;
        }

        public Long getEncuestadoId() {
            return encuestadoId;
        }

        public void setEncuestadoId(Long encuestadoId) {
            this.encuestadoId = encuestadoId;
        }

        public String getCampoValidacion() {
            return campoValidacion;
        }

        public void setCampoValidacion(String campoValidacion) {
            this.campoValidacion = campoValidacion;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }

    /**
     * {@code POST  /encuesta/init/validar} :
     * Valida el dato de la fecha de nacimiento capturado y, si es correcto,
     * verifica o genera la asociación en AplicacionEncuesta y devuelve el enlace único.
     *
     * Si ya existe la asociación y la encuesta ya fue aplicada, devuelve el mismo
     * mensaje de error que cuando se intenta usar un enlace ya completado.
     */
    @PostMapping("/init/validar")
    public ResponseEntity<?> validarEncuestadoYCrearAplicacion(@RequestBody EncuestaInitValidarRequest request) {
        LOG.debug(
            "REST request to validar encuestado for init, encuestaId: {}, encuestadoId: {}, campo: {}, valor: {}",
            request.getEncuestaId(),
            request.getEncuestadoId(),
            request.getCampoValidacion(),
            request.getValor()
        );

        if (
            request.getEncuestaId() == null ||
            request.getEncuestadoId() == null ||
            request.getCampoValidacion() == null ||
            request.getValor() == null
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Todos los datos de validación son obligatorios"));
        }

        Optional<Encuestado> encuestadoOpt = encuestadoRepository.findById(request.getEncuestadoId());
        if (encuestadoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "El encuestado especificado no existe"));
        }

        Encuestado encuestado = encuestadoOpt.get();

        if (encuestado.getFechaNacimiento() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "El encuestado no tiene registrada una fecha de nacimiento"));
        }

        LocalDate fn = encuestado.getFechaNacimiento();
        String campo = request.getCampoValidacion().toUpperCase();
        String esperado;
        switch (campo) {
            case "DIA":
                esperado = String.format("%02d", fn.getDayOfMonth());
                break;
            case "MES":
                esperado = String.format("%02d", fn.getMonthValue());
                break;
            case "ANIO":
                int anio2 = fn.getYear() % 100;
                esperado = String.format("%02d", anio2);
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campo de validación no válido"));
        }

        String valorCapturado = request.getValor().trim();
        if (!esperado.equals(valorCapturado)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "El dato capturado de la fecha de nacimiento es incorrecto"));
        }

        // Dato correcto: revisar o crear la asociación de AplicacionEncuesta
        AplicacionEncuesta aplicacionEncuesta = aplicacionEncuestaService.crearAplicacionSiNoExiste(
            request.getEncuestaId(),
            request.getEncuestadoId()
        );

        // Si ya estaba aplicada, reutilizamos la lógica de mensaje de encuesta completada
        if (aplicacionEncuesta.getFechaAplicacion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Esta encuesta ya ha sido completada"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("enlaceUnico", aplicacionEncuesta.getEnlaceUnico());

        return ResponseEntity.ok(response);
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


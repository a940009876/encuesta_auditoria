package mx.com.ado.auditoria.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mx.com.ado.auditoria.domain.AplicacionEncuesta;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.repository.AplicacionEncuestaRepository;
import mx.com.ado.auditoria.repository.EncuestaRepository;
import mx.com.ado.auditoria.repository.EncuestadoRepository;
import mx.com.ado.auditoria.service.AplicacionEncuestaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link mx.com.ado.auditoria.domain.AplicacionEncuesta}.
 */
@Service
@Transactional
public class AplicacionEncuestaServiceImpl implements AplicacionEncuestaService {

    private static final Logger LOG = LoggerFactory.getLogger(AplicacionEncuestaServiceImpl.class);

    private final AplicacionEncuestaRepository aplicacionEncuestaRepository;
    private final EncuestaRepository encuestaRepository;
    private final EncuestadoRepository encuestadoRepository;

    public AplicacionEncuestaServiceImpl(
        AplicacionEncuestaRepository aplicacionEncuestaRepository,
        EncuestaRepository encuestaRepository,
        EncuestadoRepository encuestadoRepository
    ) {
        this.aplicacionEncuestaRepository = aplicacionEncuestaRepository;
        this.encuestaRepository = encuestaRepository;
        this.encuestadoRepository = encuestadoRepository;
    }

    @Override
    public AplicacionEncuesta save(AplicacionEncuesta aplicacionEncuesta) {
        LOG.debug("Request to save AplicacionEncuesta : {}", aplicacionEncuesta);
        return aplicacionEncuestaRepository.save(aplicacionEncuesta);
    }

    @Override
    public AplicacionEncuesta update(AplicacionEncuesta aplicacionEncuesta) {
        LOG.debug("Request to update AplicacionEncuesta : {}", aplicacionEncuesta);
        return aplicacionEncuestaRepository.save(aplicacionEncuesta);
    }

    @Override
    public Optional<AplicacionEncuesta> partialUpdate(AplicacionEncuesta aplicacionEncuesta) {
        LOG.debug("Request to partially update AplicacionEncuesta : {}", aplicacionEncuesta);

        return aplicacionEncuestaRepository
            .findById(aplicacionEncuesta.getId())
            .map(existingAplicacionEncuesta -> {
                if (aplicacionEncuesta.getEnlaceUnico() != null) {
                    existingAplicacionEncuesta.setEnlaceUnico(aplicacionEncuesta.getEnlaceUnico());
                }
                if (aplicacionEncuesta.getFechaAplicacion() != null) {
                    existingAplicacionEncuesta.setFechaAplicacion(aplicacionEncuesta.getFechaAplicacion());
                }
                if (aplicacionEncuesta.getRespuestaEncuesta() != null) {
                    existingAplicacionEncuesta.setRespuestaEncuesta(aplicacionEncuesta.getRespuestaEncuesta());
                }

                return existingAplicacionEncuesta;
            })
            .map(aplicacionEncuestaRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AplicacionEncuesta> findAll(Pageable pageable) {
        LOG.debug("Request to get all AplicacionEncuestas");
        return aplicacionEncuestaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AplicacionEncuesta> findOne(Long id) {
        LOG.debug("Request to get AplicacionEncuesta : {}", id);
        return aplicacionEncuestaRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AplicacionEncuesta : {}", id);
        aplicacionEncuestaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AplicacionEncuesta> findByEnlaceUnico(String enlaceUnico) {
        LOG.info("Service: Buscando AplicacionEncuesta por enlaceUnico: {}", enlaceUnico);
        try {
            Optional<AplicacionEncuesta> result = aplicacionEncuestaRepository.findByEnlaceUnico(enlaceUnico);
            if (result.isPresent()) {
                LOG.info("Service: AplicacionEncuesta encontrada - ID: {}", result.get().getId());
            } else {
                LOG.warn("Service: No se encontró AplicacionEncuesta con enlaceUnico: {}", enlaceUnico);
            }
            return result;
        } catch (Exception e) {
            LOG.error("Service: Error al buscar AplicacionEncuesta por enlaceUnico: {}", enlaceUnico, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public int generarEnlacesParaEncuestados(Long encuestaId) {
        LOG.debug("Request to generate enlaces for encuesta : {}", encuestaId);

        Optional<Encuesta> encuestaOpt = encuestaRepository.findById(encuestaId);
        if (encuestaOpt.isEmpty()) {
            throw new IllegalArgumentException("Encuesta no encontrada con id: " + encuestaId);
        }

        Encuesta encuesta = encuestaOpt.get();
        List<Encuestado> encuestados = encuestadoRepository.findAll();

        int count = 0;
        for (Encuestado encuestado : encuestados) {
            // Verificar si ya existe un enlace para este encuestado y encuesta
            Optional<AplicacionEncuesta> existeEnlace = aplicacionEncuestaRepository.findByEncuestadoAndEncuesta(
                encuestado.getId(),
                encuestaId
            );

            if (existeEnlace.isEmpty()) {
                AplicacionEncuesta aplicacionEncuesta = new AplicacionEncuesta();
                aplicacionEncuesta.setEncuesta(encuesta);
                aplicacionEncuesta.setEncuestado(encuestado);
                aplicacionEncuesta.setEnlaceUnico(generarCodigoUnico());

                aplicacionEncuestaRepository.save(aplicacionEncuesta);
                count++;
            }
        }

        LOG.info("Generated {} enlaces for encuesta {}", count, encuestaId);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AplicacionEncuesta> findCompletadasByEncuestaId(Long encuestaId) {
        LOG.debug("Request to get completed AplicacionEncuestas for encuesta : {}", encuestaId);
        return aplicacionEncuestaRepository.findCompletadasByEncuestaId(encuestaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AplicacionEncuesta> findByEncuestadoAndEncuesta(Long encuestadoId, Long encuestaId) {
        LOG.debug("Request to get AplicacionEncuesta by encuestado {} and encuesta {}", encuestadoId, encuestaId);
        return aplicacionEncuestaRepository.findByEncuestadoAndEncuesta(encuestadoId, encuestaId);
    }

    @Override
    @Transactional
    public AplicacionEncuesta crearAplicacionSiNoExiste(Long encuestaId, Long encuestadoId) {
        LOG.debug("Request to create AplicacionEncuesta if not exists for encuesta {} and encuestado {}", encuestaId, encuestadoId);

        Optional<AplicacionEncuesta> existenteOpt = aplicacionEncuestaRepository.findByEncuestadoAndEncuesta(encuestadoId, encuestaId);
        if (existenteOpt.isPresent()) {
            LOG.debug("AplicacionEncuesta already exists for encuesta {} and encuestado {}", encuestaId, encuestadoId);
            return existenteOpt.get();
        }

        Encuesta encuesta = encuestaRepository
            .findById(encuestaId)
            .orElseThrow(() -> new IllegalArgumentException("Encuesta no encontrada con id: " + encuestaId));

        Encuestado encuestado = encuestadoRepository
            .findById(encuestadoId)
            .orElseThrow(() -> new IllegalArgumentException("Encuestado no encontrado con id: " + encuestadoId));

        AplicacionEncuesta aplicacionEncuesta = new AplicacionEncuesta();
        aplicacionEncuesta.setEncuesta(encuesta);
        aplicacionEncuesta.setEncuestado(encuestado);
        aplicacionEncuesta.setEnlaceUnico(generarCodigoUnico());

        AplicacionEncuesta guardada = aplicacionEncuestaRepository.save(aplicacionEncuesta);
        LOG.debug("Created AplicacionEncuesta {} for encuesta {} and encuestado {}", guardada.getId(), encuestaId, encuestadoId);
        return guardada;
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString();
        } while (aplicacionEncuestaRepository.findByEnlaceUnico(codigo).isPresent());
        return codigo;
    }
}

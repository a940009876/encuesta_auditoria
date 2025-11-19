package mx.com.ado.auditoria.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuesta;
import mx.com.ado.auditoria.repository.EncuestaRepository;
import mx.com.ado.auditoria.service.EncuestaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link mx.com.ado.auditoria.domain.Encuesta}.
 */
@Service
@Transactional
public class EncuestaServiceImpl implements EncuestaService {

    private static final Logger LOG = LoggerFactory.getLogger(EncuestaServiceImpl.class);

    private final EncuestaRepository encuestaRepository;

    public EncuestaServiceImpl(EncuestaRepository encuestaRepository) {
        this.encuestaRepository = encuestaRepository;
    }

    @Override
    public Encuesta save(Encuesta encuesta) {
        LOG.debug("Request to save Encuesta : {}", encuesta);
        return encuestaRepository.save(encuesta);
    }

    @Override
    public Encuesta update(Encuesta encuesta) {
        LOG.debug("Request to update Encuesta : {}", encuesta);
        return encuestaRepository.save(encuesta);
    }

    @Override
    public Optional<Encuesta> partialUpdate(Encuesta encuesta) {
        LOG.debug("Request to partially update Encuesta : {}", encuesta);

        return encuestaRepository
            .findById(encuesta.getId())
            .map(existingEncuesta -> {
                if (encuesta.getNombre() != null) {
                    existingEncuesta.setNombre(encuesta.getNombre());
                }
                if (encuesta.getVigenciaDesde() != null) {
                    existingEncuesta.setVigenciaDesde(encuesta.getVigenciaDesde());
                }
                if (encuesta.getVigenciaHasta() != null) {
                    existingEncuesta.setVigenciaHasta(encuesta.getVigenciaHasta());
                }
                if (encuesta.getStructuraJson() != null) {
                    existingEncuesta.setStructuraJson(encuesta.getStructuraJson());
                }

                return existingEncuesta;
            })
            .map(encuestaRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Encuesta> findAll(Pageable pageable) {
        LOG.debug("Request to get all Encuestas");
        return encuestaRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Encuesta> findOne(Long id) {
        LOG.debug("Request to get Encuesta : {}", id);
        return encuestaRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Encuesta : {}", id);
        encuestaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Encuesta> findVigentesOrderByIdDesc() {
        LOG.debug("Request to get all vigentes Encuestas");
        return encuestaRepository.findVigentesOrderByIdDesc(LocalDate.now());
    }
}

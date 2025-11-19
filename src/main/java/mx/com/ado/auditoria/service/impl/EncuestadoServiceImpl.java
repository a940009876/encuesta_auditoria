package mx.com.ado.auditoria.service.impl;

import java.util.Optional;
import mx.com.ado.auditoria.domain.Encuestado;
import mx.com.ado.auditoria.repository.EncuestadoRepository;
import mx.com.ado.auditoria.service.EncuestadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link mx.com.ado.auditoria.domain.Encuestado}.
 */
@Service
@Transactional
public class EncuestadoServiceImpl implements EncuestadoService {

    private static final Logger LOG = LoggerFactory.getLogger(EncuestadoServiceImpl.class);

    private final EncuestadoRepository encuestadoRepository;

    public EncuestadoServiceImpl(EncuestadoRepository encuestadoRepository) {
        this.encuestadoRepository = encuestadoRepository;
    }

    @Override
    public Encuestado save(Encuestado encuestado) {
        LOG.debug("Request to save Encuestado : {}", encuestado);
        return encuestadoRepository.save(encuestado);
    }

    @Override
    public Encuestado update(Encuestado encuestado) {
        LOG.debug("Request to update Encuestado : {}", encuestado);
        return encuestadoRepository.save(encuestado);
    }

    @Override
    public Optional<Encuestado> partialUpdate(Encuestado encuestado) {
        LOG.debug("Request to partially update Encuestado : {}", encuestado);

        return encuestadoRepository
            .findById(encuestado.getId())
            .map(existingEncuestado -> {
                if (encuestado.getNombre() != null) {
                    existingEncuestado.setNombre(encuestado.getNombre());
                }
                if (encuestado.getFechaNacimiento() != null) {
                    existingEncuestado.setFechaNacimiento(encuestado.getFechaNacimiento());
                }
                if (encuestado.getClaveEmpleado() != null) {
                    existingEncuestado.setClaveEmpleado(encuestado.getClaveEmpleado());
                }

                return existingEncuestado;
            })
            .map(encuestadoRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Encuestado> findAll(Pageable pageable) {
        LOG.debug("Request to get all Encuestados");
        return encuestadoRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Encuestado> findOne(Long id) {
        LOG.debug("Request to get Encuestado : {}", id);
        return encuestadoRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Encuestado : {}", id);
        encuestadoRepository.deleteById(id);
    }
}

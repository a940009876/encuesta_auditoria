import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { deleteEntity, getEntity } from './aplicacion-encuesta.reducer';

export const AplicacionEncuestaDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const aplicacionEncuestaEntity = useAppSelector(state => state.aplicacionEncuesta.entity);
  const updateSuccess = useAppSelector(state => state.aplicacionEncuesta.updateSuccess);

  const handleClose = () => {
    navigate(`/aplicacion-encuesta${pageLocation.search}`);
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(aplicacionEncuestaEntity.id));
  };

  return (
    <Modal isOpen toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="aplicacionEncuestaDeleteDialogHeading">
        Confirmar operación de borrado
      </ModalHeader>
      <ModalBody id="auditoriaApp.aplicacionEncuesta.delete.question">
        ¿Seguro que quiere eliminar Aplicacion Encuesta {aplicacionEncuestaEntity.id}?
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancelar
        </Button>
        <Button id="jhi-confirm-delete-aplicacionEncuesta" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Eliminar
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default AplicacionEncuestaDeleteDialog;

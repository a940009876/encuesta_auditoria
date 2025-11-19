import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './encuestado.reducer';

export const EncuestadoUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const encuestadoEntity = useAppSelector(state => state.encuestado.entity);
  const loading = useAppSelector(state => state.encuestado.loading);
  const updating = useAppSelector(state => state.encuestado.updating);
  const updateSuccess = useAppSelector(state => state.encuestado.updateSuccess);

  const handleClose = () => {
    navigate(`/encuestado${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }

    const entity = {
      ...encuestadoEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...encuestadoEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="auditoriaApp.encuestado.home.createOrEditLabel" data-cy="EncuestadoCreateUpdateHeading">
            Crear o editar Encuestado
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="encuestado-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="Nombre" id="encuestado-nombre" name="nombre" data-cy="nombre" type="text" />
              <ValidatedField
                label="Fecha Nacimiento"
                id="encuestado-fechaNacimiento"
                name="fechaNacimiento"
                data-cy="fechaNacimiento"
                type="date"
              />
              <ValidatedField
                label="Clave Empleado"
                id="encuestado-claveEmpleado"
                name="claveEmpleado"
                data-cy="claveEmpleado"
                type="text"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/encuestado" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Volver</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Guardar
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default EncuestadoUpdate;

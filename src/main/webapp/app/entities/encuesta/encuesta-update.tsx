import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './encuesta.reducer';

export const EncuestaUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const encuestaEntity = useAppSelector(state => state.encuesta.entity);
  const loading = useAppSelector(state => state.encuesta.loading);
  const updating = useAppSelector(state => state.encuesta.updating);
  const updateSuccess = useAppSelector(state => state.encuesta.updateSuccess);

  const handleClose = () => {
    navigate(`/encuesta${location.search}`);
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
      ...encuestaEntity,
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
          ...encuestaEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="auditoriaApp.encuesta.home.createOrEditLabel" data-cy="EncuestaCreateUpdateHeading">
            Crear o editar Encuesta
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="encuesta-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField label="Nombre" id="encuesta-nombre" name="nombre" data-cy="nombre" type="text" />
              <ValidatedField label="Vigencia Desde" id="encuesta-vigenciaDesde" name="vigenciaDesde" data-cy="vigenciaDesde" type="date" />
              <ValidatedField label="Vigencia Hasta" id="encuesta-vigenciaHasta" name="vigenciaHasta" data-cy="vigenciaHasta" type="date" />
              <ValidatedField
                label="Structura Json"
                id="encuesta-structuraJson"
                name="structuraJson"
                data-cy="structuraJson"
                type="textarea"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/encuesta" replace color="info">
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

export default EncuestaUpdate;

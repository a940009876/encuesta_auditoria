import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getEncuestados } from 'app/entities/encuestado/encuestado.reducer';
import { getEntities as getEncuestas } from 'app/entities/encuesta/encuesta.reducer';
import { createEntity, getEntity, reset, updateEntity } from './aplicacion-encuesta.reducer';

export const AplicacionEncuestaUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const encuestados = useAppSelector(state => state.encuestado.entities);
  const encuestas = useAppSelector(state => state.encuesta.entities);
  const aplicacionEncuestaEntity = useAppSelector(state => state.aplicacionEncuesta.entity);
  const loading = useAppSelector(state => state.aplicacionEncuesta.loading);
  const updating = useAppSelector(state => state.aplicacionEncuesta.updating);
  const updateSuccess = useAppSelector(state => state.aplicacionEncuesta.updateSuccess);

  const handleClose = () => {
    navigate(`/aplicacion-encuesta${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getEncuestados({}));
    dispatch(getEncuestas({}));
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
      ...aplicacionEncuestaEntity,
      ...values,
      encuestado: encuestados.find(it => it.id.toString() === values.encuestado?.toString()),
      encuesta: encuestas.find(it => it.id.toString() === values.encuesta?.toString()),
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
          ...aplicacionEncuestaEntity,
          encuestado: aplicacionEncuestaEntity?.encuestado?.id,
          encuesta: aplicacionEncuestaEntity?.encuesta?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="auditoriaApp.aplicacionEncuesta.home.createOrEditLabel" data-cy="AplicacionEncuestaCreateUpdateHeading">
            Crear o editar Aplicacion Encuesta
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="aplicacion-encuesta-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Enlace Unico"
                id="aplicacion-encuesta-enlaceUnico"
                name="enlaceUnico"
                data-cy="enlaceUnico"
                type="text"
              />
              <ValidatedField
                label="Fecha Aplicacion"
                id="aplicacion-encuesta-fechaAplicacion"
                name="fechaAplicacion"
                data-cy="fechaAplicacion"
                type="date"
              />
              <ValidatedField
                label="Respuesta Encuesta"
                id="aplicacion-encuesta-respuestaEncuesta"
                name="respuestaEncuesta"
                data-cy="respuestaEncuesta"
                type="textarea"
              />
              <ValidatedField id="aplicacion-encuesta-encuestado" name="encuestado" data-cy="encuestado" label="Encuestado" type="select">
                <option value="" key="0" />
                {encuestados
                  ? encuestados.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="aplicacion-encuesta-encuesta" name="encuesta" data-cy="encuesta" label="Encuesta" type="select">
                <option value="" key="0" />
                {encuestas
                  ? encuestas.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/aplicacion-encuesta" replace color="info">
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

export default AplicacionEncuestaUpdate;

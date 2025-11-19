import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './encuestado.reducer';

export const EncuestadoDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const encuestadoEntity = useAppSelector(state => state.encuestado.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="encuestadoDetailsHeading">Encuestado</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{encuestadoEntity.id}</dd>
          <dt>
            <span id="nombre">Nombre</span>
          </dt>
          <dd>{encuestadoEntity.nombre}</dd>
          <dt>
            <span id="fechaNacimiento">Fecha Nacimiento</span>
          </dt>
          <dd>
            {encuestadoEntity.fechaNacimiento ? (
              <TextFormat value={encuestadoEntity.fechaNacimiento} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="claveEmpleado">Clave Empleado</span>
          </dt>
          <dd>{encuestadoEntity.claveEmpleado}</dd>
        </dl>
        <Button tag={Link} to="/encuestado" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/encuestado/${encuestadoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
        </Button>
      </Col>
    </Row>
  );
};

export default EncuestadoDetail;

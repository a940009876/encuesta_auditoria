import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './encuesta.reducer';

export const EncuestaDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const encuestaEntity = useAppSelector(state => state.encuesta.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="encuestaDetailsHeading">Encuesta</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{encuestaEntity.id}</dd>
          <dt>
            <span id="nombre">Nombre</span>
          </dt>
          <dd>{encuestaEntity.nombre}</dd>
          <dt>
            <span id="vigenciaDesde">Vigencia Desde</span>
          </dt>
          <dd>
            {encuestaEntity.vigenciaDesde ? (
              <TextFormat value={encuestaEntity.vigenciaDesde} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="vigenciaHasta">Vigencia Hasta</span>
          </dt>
          <dd>
            {encuestaEntity.vigenciaHasta ? (
              <TextFormat value={encuestaEntity.vigenciaHasta} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="structuraJson">Structura Json</span>
          </dt>
          <dd>{encuestaEntity.structuraJson}</dd>
        </dl>
        <Button tag={Link} to="/encuesta" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/encuesta/${encuestaEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
        </Button>
      </Col>
    </Row>
  );
};

export default EncuestaDetail;

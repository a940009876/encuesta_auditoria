import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './aplicacion-encuesta.reducer';

export const AplicacionEncuestaDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const aplicacionEncuestaEntity = useAppSelector(state => state.aplicacionEncuesta.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="aplicacionEncuestaDetailsHeading">Aplicacion Encuesta</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{aplicacionEncuestaEntity.id}</dd>
          <dt>
            <span id="enlaceUnico">Enlace Unico</span>
          </dt>
          <dd>{aplicacionEncuestaEntity.enlaceUnico}</dd>
          <dt>
            <span id="fechaAplicacion">Fecha Aplicacion</span>
          </dt>
          <dd>
            {aplicacionEncuestaEntity.fechaAplicacion ? (
              <TextFormat value={aplicacionEncuestaEntity.fechaAplicacion} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="respuestaEncuesta">Respuesta Encuesta</span>
          </dt>
          <dd>{aplicacionEncuestaEntity.respuestaEncuesta}</dd>
          <dt>Encuestado</dt>
          <dd>{aplicacionEncuestaEntity.encuestado ? aplicacionEncuestaEntity.encuestado.id : ''}</dd>
          <dt>Encuesta</dt>
          <dd>{aplicacionEncuestaEntity.encuesta ? aplicacionEncuestaEntity.encuesta.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/aplicacion-encuesta" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/aplicacion-encuesta/${aplicacionEncuestaEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
        </Button>
      </Col>
    </Row>
  );
};

export default AplicacionEncuestaDetail;

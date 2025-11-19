import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './aplicacion-encuesta.reducer';

export const AplicacionEncuesta = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const aplicacionEncuestaList = useAppSelector(state => state.aplicacionEncuesta.entities);
  const loading = useAppSelector(state => state.aplicacionEncuesta.loading);
  const totalItems = useAppSelector(state => state.aplicacionEncuesta.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="aplicacion-encuesta-heading" data-cy="AplicacionEncuestaHeading">
        Aplicacion Encuestas
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refrescar lista
          </Button>
          <Link
            to="/aplicacion-encuesta/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp; Crear nuevo Aplicacion Encuesta
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {aplicacionEncuestaList && aplicacionEncuestaList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('enlaceUnico')}>
                  Enlace Unico <FontAwesomeIcon icon={getSortIconByFieldName('enlaceUnico')} />
                </th>
                <th className="hand" onClick={sort('fechaAplicacion')}>
                  Fecha Aplicacion <FontAwesomeIcon icon={getSortIconByFieldName('fechaAplicacion')} />
                </th>
                <th className="hand" onClick={sort('respuestaEncuesta')}>
                  Respuesta Encuesta <FontAwesomeIcon icon={getSortIconByFieldName('respuestaEncuesta')} />
                </th>
                <th>
                  Encuestado <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Encuesta <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {aplicacionEncuestaList.map((aplicacionEncuesta, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/aplicacion-encuesta/${aplicacionEncuesta.id}`} color="link" size="sm">
                      {aplicacionEncuesta.id}
                    </Button>
                  </td>
                  <td>{aplicacionEncuesta.enlaceUnico}</td>
                  <td>
                    {aplicacionEncuesta.fechaAplicacion ? (
                      <TextFormat type="date" value={aplicacionEncuesta.fechaAplicacion} format={APP_LOCAL_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{aplicacionEncuesta.respuestaEncuesta}</td>
                  <td>
                    {aplicacionEncuesta.encuestado ? (
                      <Link to={`/encuestado/${aplicacionEncuesta.encuestado.id}`}>{aplicacionEncuesta.encuestado.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td>
                    {aplicacionEncuesta.encuesta ? (
                      <Link to={`/encuesta/${aplicacionEncuesta.encuesta.id}`}>{aplicacionEncuesta.encuesta.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/aplicacion-encuesta/${aplicacionEncuesta.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">Vista</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/aplicacion-encuesta/${aplicacionEncuesta.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/aplicacion-encuesta/${aplicacionEncuesta.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Eliminar</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">Ningún Aplicacion Encuestas encontrado</div>
        )}
      </div>
      {totalItems ? (
        <div className={aplicacionEncuestaList && aplicacionEncuestaList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default AplicacionEncuesta;

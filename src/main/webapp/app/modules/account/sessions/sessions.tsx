import React, { useEffect } from 'react';
import { Alert, Button, Table } from 'reactstrap';

import { getSession } from 'app/shared/reducers/authentication';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { findAll, invalidateSession } from './sessions.reducer';

export const SessionsPage = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
    dispatch(findAll());
  }, []);

  const doSessionInvalidation = series => () => {
    dispatch(invalidateSession(series));
    dispatch(findAll());
  };

  const refreshList = () => {
    dispatch(findAll());
  };

  const account = useAppSelector(state => state.authentication.account);
  const sessions = useAppSelector(state => state.sessions.sessions);
  const updateSuccess = useAppSelector(state => state.sessions.updateSuccess);
  const updateFailure = useAppSelector(state => state.sessions.updateFailure);

  return (
    <div>
      <h2>
        Sesiones activas para [<strong>{account.login}</strong>]
      </h2>

      {updateSuccess ? (
        <Alert color="success">
          <strong>¡Sesión invalidada!</strong>
        </Alert>
      ) : null}

      {updateFailure ? (
        <Alert color="danger">
          <strong>¡Ha ocurrido un error!</strong> La sesión no puede ser invalidada.
        </Alert>
      ) : null}

      <Button color="primary" onClick={refreshList}>
        Refresh
      </Button>

      <div className="table-responsive">
        <Table className="table-striped">
          <thead>
            <tr>
              <th>Dirección IP</th>
              <th>Agente de usuario</th>
              <th>Fecha</th>
              <th />
            </tr>
          </thead>

          <tbody>
            {sessions.map((s, index) => (
              <tr key={index}>
                <td>{s.ipAddress}</td>
                <td>{s.userAgent}</td>
                <td>{s.tokenDate}</td>
                <td>
                  <Button color="primary" onClick={doSessionInvalidation(s.series)}>
                    Invalidar
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      </div>
    </div>
  );
};

export default SessionsPage;

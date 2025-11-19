import './home.scss';

import React from 'react';
import { Link } from 'react-router-dom';

import { Alert, Col, Row } from 'reactstrap';

import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="3" className="pad">
        <span className="hipster rounded" />
      </Col>
      <Col md="9">
        <h1 className="display-4">¡Encuestas de Auditoria!</h1>
        <p className="lead">Esta es su página de inicio</p>
        {account?.login ? (
          <div>
            <Alert color="success">Está conectado como &quot;{account.login}&quot;.</Alert>
          </div>
        ) : (
          <div>
            <Alert color="warning">
              Si desea
              <span>&nbsp;</span>
              <Link to="/login" className="alert-link">
                iniciar sesión
              </Link>
              , puede intentar con las cuentas predeterminadas:
              <br />- Administrador (usuario=&quot;admin&quot; y contraseña=&quot;admin&quot;) <br />- Usuario (usuario=&quot;user&quot; y
              contraseña=&quot;user&quot;).
            </Alert>

            <Alert color="warning">
              ¿Aún no tienes una cuenta?&nbsp;
              <Link to="/account/register" className="alert-link">
                Crea una cuenta
              </Link>
            </Alert>
          </div>
        )}
      </Col>
    </Row>
  );
};

export default Home;

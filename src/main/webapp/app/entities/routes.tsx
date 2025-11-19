import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Encuestado from './encuestado';
import Encuesta from './encuesta';
import AplicacionEncuesta from './aplicacion-encuesta';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="encuestado/*" element={<Encuestado />} />
        <Route path="encuesta/*" element={<Encuesta />} />
        <Route path="aplicacion-encuesta/*" element={<AplicacionEncuesta />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};

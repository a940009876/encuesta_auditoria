import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import AplicacionEncuesta from './aplicacion-encuesta';
import AplicacionEncuestaDetail from './aplicacion-encuesta-detail';
import AplicacionEncuestaUpdate from './aplicacion-encuesta-update';
import AplicacionEncuestaDeleteDialog from './aplicacion-encuesta-delete-dialog';

const AplicacionEncuestaRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<AplicacionEncuesta />} />
    <Route path="new" element={<AplicacionEncuestaUpdate />} />
    <Route path=":id">
      <Route index element={<AplicacionEncuestaDetail />} />
      <Route path="edit" element={<AplicacionEncuestaUpdate />} />
      <Route path="delete" element={<AplicacionEncuestaDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AplicacionEncuestaRoutes;

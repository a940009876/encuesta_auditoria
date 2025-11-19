import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Encuestado from './encuestado';
import EncuestadoDetail from './encuestado-detail';
import EncuestadoUpdate from './encuestado-update';
import EncuestadoDeleteDialog from './encuestado-delete-dialog';

const EncuestadoRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Encuestado />} />
    <Route path="new" element={<EncuestadoUpdate />} />
    <Route path=":id">
      <Route index element={<EncuestadoDetail />} />
      <Route path="edit" element={<EncuestadoUpdate />} />
      <Route path="delete" element={<EncuestadoDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default EncuestadoRoutes;

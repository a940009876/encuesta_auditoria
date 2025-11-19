import React from 'react';

import { Route } from 'react-router';
import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import UserManagement from './user-management';
import Logs from './logs/logs';
import Health from './health/health';
import Metrics from './metrics/metrics';
import Configuration from './configuration/configuration';
import Docs from './docs/docs';
import GenerarEnlaces from './generar-enlaces';
import ExportarEncuesta from './exportar-encuesta';

const AdministrationRoutes = () => (
  <div>
    <ErrorBoundaryRoutes>
      <Route path="user-management/*" element={<UserManagement />} />
      <Route path="health" element={<Health />} />
      <Route path="metrics" element={<Metrics />} />
      <Route path="configuration" element={<Configuration />} />
      <Route path="logs" element={<Logs />} />
      <Route path="docs" element={<Docs />} />
      <Route path="generar-enlaces" element={<GenerarEnlaces />} />
      <Route path="exportar-encuesta" element={<ExportarEncuesta />} />
    </ErrorBoundaryRoutes>
  </div>
);

export default AdministrationRoutes;

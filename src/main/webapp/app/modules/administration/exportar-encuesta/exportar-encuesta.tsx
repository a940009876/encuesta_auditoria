import React, { useEffect, useState } from 'react';
import { Button, Card, CardBody, CardHeader, Col, FormGroup, Input, Label, Row, Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSpinner, faDownload } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import { IEncuesta } from 'app/shared/model/encuesta.model';

export const ExportarEncuesta = () => {
  const [encuestas, setEncuestas] = useState<IEncuesta[]>([]);
  const [selectedEncuestaId, setSelectedEncuestaId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message?: string } | null>(null);

  useEffect(() => {
    loadEncuestas();
  }, []);

  const loadEncuestas = async () => {
    setLoading(true);
    try {
      const response = await axios.get<IEncuesta[]>('api/encuestas?sort=id,desc&size=1000');
      const encuestasData = response.data;
      setEncuestas(encuestasData);
      if (encuestasData.length > 0) {
        setSelectedEncuestaId(encuestasData[0].id || null);
      }
    } catch (error) {
      console.error('Error loading encuestas:', error);
      setResult({
        success: false,
        message: 'Error al cargar las encuestas',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    if (!selectedEncuestaId) {
      setResult({
        success: false,
        message: 'Por favor seleccione una encuesta',
      });
      return;
    }

    setExporting(true);
    setResult(null);
    try {
      const response = await axios.get(`api/admin/exportar-encuesta/${selectedEncuestaId}`, {
        responseType: 'blob',
      });

      const selectedEncuesta = encuestas.find(e => e.id === selectedEncuestaId);
      const fileName = selectedEncuesta
        ? `${selectedEncuesta.nombre.replace(/\s+/g, '_').toLowerCase()}.csv`
        : `encuesta_${selectedEncuestaId}.csv`;

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      setResult({
        success: true,
        message: 'Archivo CSV generado y descargado exitosamente',
      });
    } catch (error: any) {
      console.error('Error exporting encuesta:', error);
      setResult({
        success: false,
        message: error.response?.data?.message || 'Error al exportar la encuesta',
      });
    } finally {
      setExporting(false);
    }
  };

  return (
    <div>
      <Row>
        <Col md="10">
          <Card>
            <CardHeader>
              <h2 id="exportar-encuesta-heading">Exportar Encuesta</h2>
            </CardHeader>
            <CardBody>
              <FormGroup>
                <Label for="encuesta-select">Seleccione la encuesta a exportar:</Label>
                <Input
                  type="select"
                  name="encuesta"
                  id="encuesta-select"
                  value={selectedEncuestaId || ''}
                  onChange={e => setSelectedEncuestaId(e.target.value ? Number(e.target.value) : null)}
                  disabled={loading || exporting}
                >
                  <option value="">-- Seleccione una encuesta --</option>
                  {encuestas.map(encuesta => (
                    <option key={encuesta.id} value={encuesta.id}>
                      {encuesta.nombre}
                    </option>
                  ))}
                </Input>
              </FormGroup>

              {result && (
                <Alert color={result.success ? 'success' : 'danger'} className="mt-3">
                  {result.message}
                </Alert>
              )}

              <div className="mt-4">
                <Button color="primary" onClick={handleExport} disabled={!selectedEncuestaId || loading || exporting}>
                  {exporting ? (
                    <>
                      <FontAwesomeIcon icon={faSpinner} spin /> Exportando...
                    </>
                  ) : (
                    <>
                      <FontAwesomeIcon icon={faDownload} /> Exportar
                    </>
                  )}
                </Button>
                <Button color="secondary" className="ml-2" onClick={loadEncuestas} disabled={loading || exporting}>
                  Recargar
                </Button>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default ExportarEncuesta;

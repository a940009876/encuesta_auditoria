import React, { useEffect, useState } from 'react';
import { Button, Card, CardBody, CardHeader, Col, FormGroup, Input, Label, Row, Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSpinner } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import dayjs from 'dayjs';
import { IEncuesta } from 'app/shared/model/encuesta.model';
import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export const GenerarEnlaces = () => {
  const [encuestas, setEncuestas] = useState<IEncuesta[]>([]);
  const [selectedEncuestaId, setSelectedEncuestaId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState<{ success: boolean; count?: number; message?: string } | null>(null);

  useEffect(() => {
    loadEncuestasVigentes();
  }, []);

  const loadEncuestasVigentes = async () => {
    setLoading(true);
    try {
      const response = await axios.get<IEncuesta[]>('api/admin/encuestas-vigentes');
      const encuestasData = response.data;
      setEncuestas(encuestasData);
      if (encuestasData.length > 0) {
        // Seleccionar la última encuesta por defecto (primera en la lista ya que viene ordenada desc)
        setSelectedEncuestaId(encuestasData[0].id || null);
      }
    } catch (error) {
      console.error('Error loading encuestas:', error);
      setResult({
        success: false,
        message: 'Error al cargar las encuestas vigentes',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleGenerar = async () => {
    if (!selectedEncuestaId) {
      setResult({
        success: false,
        message: 'Por favor seleccione una encuesta',
      });
      return;
    }

    setGenerating(true);
    setResult(null);
    try {
      const response = await axios.post<number>(`api/admin/generar-enlaces/${selectedEncuestaId}`);
      const count = response.data;
      setResult({
        success: true,
        count,
        message: `Se generaron ${count} enlaces exitosamente`,
      });
    } catch (error) {
      console.error('Error generating enlaces:', error);
      setResult({
        success: false,
        message: 'Error al generar los enlaces',
      });
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div>
      <Row>
        <Col md="8">
          <Card>
            <CardHeader>
              <h2 id="generar-enlaces-heading">Generar Enlaces Encuesta</h2>
            </CardHeader>
            <CardBody>
              <FormGroup>
                <Label for="encuesta-select">Seleccione la encuesta a aplicar:</Label>
                <Input
                  type="select"
                  name="encuesta"
                  id="encuesta-select"
                  value={selectedEncuestaId || ''}
                  onChange={e => setSelectedEncuestaId(e.target.value ? Number(e.target.value) : null)}
                  disabled={loading || generating}
                >
                  <option value="">-- Seleccione una encuesta --</option>
                  {encuestas.map(encuesta => {
                    const vigenciaHastaFormatted = encuesta.vigenciaHasta
                      ? dayjs(encuesta.vigenciaHasta).isValid()
                        ? dayjs(encuesta.vigenciaHasta).format(APP_LOCAL_DATE_FORMAT)
                        : String(encuesta.vigenciaHasta)
                      : '';
                    return (
                      <option key={encuesta.id} value={encuesta.id}>
                        {encuesta.nombre}
                        {vigenciaHastaFormatted ? ` (Vigente hasta: ${vigenciaHastaFormatted})` : ''}
                      </option>
                    );
                  })}
                </Input>
              </FormGroup>

              {result && (
                <Alert color={result.success ? 'success' : 'danger'} className="mt-3">
                  {result.message}
                  {result.success && result.count !== undefined && (
                    <div className="mt-2">
                      <strong>Total de enlaces creados: {result.count}</strong>
                    </div>
                  )}
                </Alert>
              )}

              <div className="mt-4">
                <Button color="primary" onClick={handleGenerar} disabled={!selectedEncuestaId || loading || generating}>
                  {generating ? (
                    <>
                      <FontAwesomeIcon icon={faSpinner} spin /> Generando...
                    </>
                  ) : (
                    'Generar'
                  )}
                </Button>
                <Button color="secondary" className="ml-2" onClick={loadEncuestasVigentes} disabled={loading || generating}>
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

export default GenerarEnlaces;

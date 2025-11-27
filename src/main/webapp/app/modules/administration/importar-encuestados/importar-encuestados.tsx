import React, { useState } from 'react';
import { Button, Card, CardBody, CardHeader, Col, FormGroup, Input, Label, Row, Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSpinner, faUpload } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';

interface IRegistroConProblema {
  lineaOriginal: string;
  motivo: string;
}

interface IImportEncuestadosResult {
  registrosImportados: number;
  registrosConProblemas: IRegistroConProblema[];
}

export const ImportarEncuestados = () => {
  const [file, setFile] = useState<File | null>(null);
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<IImportEncuestadosResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      setFile(event.target.files[0]);
      setResult(null);
      setError(null);
    }
  };

  const handleImport = async () => {
    if (!file) {
      setError('Por favor seleccione un archivo CSV.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setImporting(true);
    setResult(null);
    setError(null);

    try {
      const response = await axios.post<IImportEncuestadosResult>('api/admin/importar-encuestados', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setResult(response.data);
    } catch (e: any) {
      // eslint-disable-next-line no-console
      console.error('Error importing encuestados:', e);
      const message = e.response?.data?.message || 'Error al importar los encuestados';
      setError(message);
    } finally {
      setImporting(false);
    }
  };

  return (
    <div>
      <Row>
        <Col md="10">
          <Card>
            <CardHeader>
              <h2 id="importar-encuestados-heading">Importar Encuestados</h2>
            </CardHeader>
            <CardBody>
              <FormGroup>
                <Label for="file-input">Seleccione el archivo CSV a importar:</Label>
                <Input type="file" id="file-input" name="file" accept=".csv,text/csv" onChange={handleFileChange} disabled={importing} />
                <small className="form-text text-muted">Estructura esperada: Nombre,Clave,Fecha de Nacimiento (formato dd/MM/yyyy)</small>
              </FormGroup>

              {error && (
                <Alert color="danger" className="mt-3">
                  {error}
                </Alert>
              )}

              {result && (
                <Alert color="info" className="mt-3">
                  <div>
                    <strong>Registros importados:&nbsp;{result.registrosImportados}</strong>
                  </div>
                  {result.registrosConProblemas && result.registrosConProblemas.length > 0 && (
                    <div className="mt-3">
                      <div>
                        <strong>Registros con problemas:</strong>
                      </div>
                      <ul className="mt-2">
                        {result.registrosConProblemas.map((r, index) => (
                          <li key={index}>
                            {r.lineaOriginal} --------- {r.motivo}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </Alert>
              )}

              <div className="mt-4">
                <Button color="primary" onClick={handleImport} disabled={!file || importing}>
                  {importing ? (
                    <>
                      <FontAwesomeIcon icon={faSpinner} spin /> Importando...
                    </>
                  ) : (
                    <>
                      <FontAwesomeIcon icon={faUpload} /> Importar
                    </>
                  )}
                </Button>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default ImportarEncuestados;

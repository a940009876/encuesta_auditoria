import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, CardBody, CardHeader, Col, Form, FormGroup, Input, Label, Row, Spinner } from 'reactstrap';
import axios from 'axios';

interface IEncuestaVigente {
  id: number;
  nombre: string;
}

interface IBuscarResponse {
  encuestaId: number;
  encuestadoId: number;
  campoValidacion: 'DIA' | 'MES' | 'ANIO';
}

export const EncuestaInit = () => {
  const navigate = useNavigate();

  const [encuestas, setEncuestas] = useState<IEncuestaVigente[]>([]);
  const [loadingEncuestas, setLoadingEncuestas] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [encuestaId, setEncuestaId] = useState<number | null>(null);
  const [nombre, setNombre] = useState<string>('');
  const [claveEmpleado, setClaveEmpleado] = useState<string>('');

  const [buscarLoading, setBuscarLoading] = useState<boolean>(false);
  const [validacionInfo, setValidacionInfo] = useState<IBuscarResponse | null>(null);
  const [valorCandado, setValorCandado] = useState<string>('');
  const [validando, setValidando] = useState<boolean>(false);

  useEffect(() => {
    const loadEncuestas = async () => {
      try {
        setLoadingEncuestas(true);
        setError(null);
        const response = await axios.get<IEncuestaVigente[]>('api/encuesta/init/encuestas-vigentes');
        const data = response.data || [];
        setEncuestas(data);
        if (data.length > 0) {
          // Preseleccionar la más antigua (ya viene de más antigua a más reciente)
          setEncuestaId(data[0].id);
        }
      } catch (e: any) {
        setError('Error al cargar las encuestas vigentes');
      } finally {
        setLoadingEncuestas(false);
      }
    };

    loadEncuestas();
  }, []);

  const puedeAplicar = encuestaId !== null && nombre.trim().length > 0 && claveEmpleado.trim().length > 0;

  const handleAplicarEncuesta = async (event?: React.FormEvent) => {
    if (event) {
      event.preventDefault();
    }
    if (!puedeAplicar || encuestaId === null) {
      return;
    }

    try {
      setBuscarLoading(true);
      setError(null);
      setValidacionInfo(null);
      setValorCandado('');

      const payload = {
        encuestaId,
        nombre: nombre.trim(),
        claveEmpleado: claveEmpleado.trim(),
      };

      const response = await axios.post<IBuscarResponse>('api/encuesta/init/buscar-encuestado', payload);
      setValidacionInfo(response.data);
    } catch (e: any) {
      if (e.response?.data?.error) {
        setError(e.response.data.error);
      } else {
        setError('Error al buscar al encuestado. Verifique los datos capturados.');
      }
    } finally {
      setBuscarLoading(false);
    }
  };

  const getEtiquetaCandado = () => {
    if (!validacionInfo) {
      return '';
    }
    switch (validacionInfo.campoValidacion) {
      case 'DIA':
        return 'Capture el día (2 dígitos) de su fecha de nacimiento';
      case 'MES':
        return 'Capture el mes (2 dígitos) de su fecha de nacimiento';
      case 'ANIO':
        return 'Capture los dos últimos dígitos del año de su fecha de nacimiento';
      default:
        return 'Capture el dato solicitado de su fecha de nacimiento';
    }
  };

  const handleValidar = async () => {
    if (!validacionInfo || valorCandado.trim().length !== 2) {
      setError('Debe capturar exactamente 2 dígitos para la validación.');
      return;
    }

    try {
      setValidando(true);
      setError(null);

      const payload = {
        encuestaId: validacionInfo.encuestaId,
        encuestadoId: validacionInfo.encuestadoId,
        campoValidacion: validacionInfo.campoValidacion,
        valor: valorCandado.trim(),
      };

      const response = await axios.post<{ enlaceUnico: string }>('api/encuesta/init/validar', payload);
      const enlaceUnico = response.data.enlaceUnico;
      if (enlaceUnico) {
        navigate(`/encuesta/${enlaceUnico}`);
      } else {
        setError('No se recibió el enlace de la encuesta.');
      }
    } catch (e: any) {
      if (e.response?.data?.error) {
        setError(e.response.data.error);
      } else {
        setError('Error al validar la información de fecha de nacimiento.');
      }
    } finally {
      setValidando(false);
    }
  };

  if (loadingEncuestas) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        <Spinner color="primary" />
      </div>
    );
  }

  return (
    <Row>
      <Col md="8" className="mx-auto">
        <Card>
          <CardHeader>
            <h2>Inicio de Encuesta</h2>
          </CardHeader>
          <CardBody>
            {error && (
              <Alert color="danger" toggle={() => setError(null)}>
                {error}
              </Alert>
            )}
            {encuestas.length === 0 ? (
              <Alert color="warning">No hay encuestas vigentes disponibles en este momento.</Alert>
            ) : (
              <>
                <Form onSubmit={handleAplicarEncuesta}>
                  <FormGroup>
                    <Label htmlFor="encuesta-select">
                      <strong>Encuesta vigente</strong>
                    </Label>
                    <Input
                      type="select"
                      id="encuesta-select"
                      value={encuestaId ?? ''}
                      onChange={e => setEncuestaId(e.target.value ? Number(e.target.value) : null)}
                    >
                      {encuestas.map(e => (
                        <option key={e.id} value={e.id}>
                          {e.nombre}
                        </option>
                      ))}
                    </Input>
                  </FormGroup>
                  <FormGroup>
                    <Label htmlFor="nombre-input">
                      <strong>Nombre</strong>
                    </Label>
                    <Input
                      id="nombre-input"
                      type="text"
                      value={nombre}
                      onChange={e => setNombre(e.target.value)}
                      placeholder="Capture su nombre"
                    />
                  </FormGroup>
                  <FormGroup>
                    <Label htmlFor="clave-input">
                      <strong>Clave de empleado</strong>
                    </Label>
                    <Input
                      id="clave-input"
                      type="text"
                      value={claveEmpleado}
                      onChange={e => setClaveEmpleado(e.target.value)}
                      placeholder="Capture su clave de empleado"
                    />
                  </FormGroup>
                  <Button type="submit" color="primary" disabled={!puedeAplicar || buscarLoading}>
                    {buscarLoading ? 'Buscando...' : 'Aplicar encuesta'}
                  </Button>
                </Form>

                {validacionInfo && (
                  <div className="mt-4">
                    <h5>Validación de identidad</h5>
                    <FormGroup>
                      <Label htmlFor="candado-input">
                        <strong>{getEtiquetaCandado()}</strong>
                      </Label>
                      <Input
                        id="candado-input"
                        type="text"
                        maxLength={2}
                        value={valorCandado}
                        onChange={e => setValorCandado(e.target.value.replace(/\D/g, ''))}
                        placeholder="__"
                      />
                    </FormGroup>
                    <Button color="success" onClick={handleValidar} disabled={validando}>
                      {validando ? 'Validando...' : 'Validar'}
                    </Button>
                  </div>
                )}
              </>
            )}
          </CardBody>
        </Card>
      </Col>
    </Row>
  );
};

export default EncuestaInit;

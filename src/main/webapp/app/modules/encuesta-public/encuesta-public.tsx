import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Button, Card, CardBody, CardHeader, Col, Row, Alert, Spinner, Input, Label } from 'reactstrap';
import axios from 'axios';
import { IEncuestaData, IRespuestasEncuesta, IRespuesta, IPregunta, IOpcion } from 'app/shared/model/encuesta-structure.model';

export const EncuestaPublic = () => {
  const { enlaceUnico } = useParams<'enlaceUnico'>();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [encuestaData, setEncuestaData] = useState<IEncuestaData | null>(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [respuestas, setRespuestas] = useState<IRespuestasEncuesta>({ respuestas: [] });
  const [submitting, setSubmitting] = useState(false);
  const [completed, setCompleted] = useState(false);
  const [textoRespuestas, setTextoRespuestas] = useState<{ [preguntaIndice: number]: string }>({});
  const [esperandoTexto, setEsperandoTexto] = useState<{ preguntaIndice: number; opcionIndice: number; siguiente?: number } | null>(null);

  useEffect(() => {
    if (enlaceUnico) {
      loadEncuesta();
    }
  }, [enlaceUnico]);

  // Sincronizar el texto de respuestas guardadas con el estado local
  useEffect(() => {
    if (encuestaData && respuestas.respuestas.length > 0) {
      const textoActualizado: { [preguntaIndice: number]: string } = {};
      respuestas.respuestas.forEach(respuesta => {
        if (respuesta.respuesta_texto) {
          textoActualizado[respuesta.pregunta] = respuesta.respuesta_texto;
        }
      });
      setTextoRespuestas(prev => ({ ...prev, ...textoActualizado }));
    }
  }, [respuestas, encuestaData]);

  // Restaurar el texto cuando se activa el modo de espera de texto
  useEffect(() => {
    if (esperandoTexto && respuestas.respuestas.length > 0) {
      const respuestaExistente = respuestas.respuestas.find(
        r => r.pregunta === esperandoTexto.preguntaIndice && r.respuesta === esperandoTexto.opcionIndice,
      );
      if (respuestaExistente?.respuesta_texto) {
        setTextoRespuestas(prev => ({
          ...prev,
          [esperandoTexto.preguntaIndice]: respuestaExistente.respuesta_texto,
        }));
      }
    }
  }, [esperandoTexto]);

  // Limpiar el estado de espera cuando cambia la pregunta actual (si no es la misma)
  useEffect(() => {
    if (esperandoTexto && encuestaData) {
      const currentQuestion = encuestaData.encuesta.preguntas[currentQuestionIndex];
      if (currentQuestion && currentQuestion.indice !== esperandoTexto.preguntaIndice) {
        setEsperandoTexto(null);
      }
    }
    // eslint-disable-next-line
  }, [currentQuestionIndex, encuestaData]);

  const loadEncuesta = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axios.get<IEncuestaData>(`api/encuesta/${enlaceUnico}`);
      setEncuestaData(response.data);
      setLoading(false);
    } catch (err: any) {
      setLoading(false);
      if (err.response?.status === 404) {
        setError('Encuesta no encontrada');
      } else if (err.response?.status === 400) {
        setError(err.response.data?.error || 'Esta encuesta ya ha sido completada');
      } else {
        setError('Error al cargar la encuesta');
      }
    }
  };

  const handleAnswer = (preguntaIndice: number, opcionIndice: number, siguiente?: number) => {
    if (!encuestaData) {
      return;
    }

    // Buscar la opción seleccionada para verificar si tiene la bandera abierta
    const currentQuestion = encuestaData.encuesta.preguntas.find(p => p.indice === preguntaIndice);
    const opcionSeleccionada = currentQuestion?.opciones.find(o => o.indice === opcionIndice);

    // Si la opción tiene la bandera abierta, mostrar el campo de texto y esperar
    if (opcionSeleccionada?.abierta === 1) {
      // Guardar la respuesta parcial (solo la opción seleccionada)
      const nuevaRespuesta: IRespuesta = {
        pregunta: preguntaIndice,
        respuesta: opcionIndice,
      };

      // Actualizar o agregar respuesta
      const respuestasActualizadas = respuestas.respuestas.filter(r => r.pregunta !== preguntaIndice);
      respuestasActualizadas.push(nuevaRespuesta);
      setRespuestas({ respuestas: respuestasActualizadas });

      // Activar el modo de espera de texto
      setEsperandoTexto({ preguntaIndice, opcionIndice, siguiente });
      return;
    }

    // Si no es abierta, proceder normalmente
    const nuevaRespuesta: IRespuesta = {
      pregunta: preguntaIndice,
      respuesta: opcionIndice,
    };

    // Actualizar o agregar respuesta
    const respuestasActualizadas = respuestas.respuestas.filter(r => r.pregunta !== preguntaIndice);
    respuestasActualizadas.push(nuevaRespuesta);
    setRespuestas({ respuestas: respuestasActualizadas });

    // Determinar la siguiente pregunta
    if (siguiente !== undefined) {
      const siguienteIndex = encuestaData.encuesta.preguntas.findIndex(p => p.indice === siguiente);
      if (siguienteIndex !== -1) {
        setCurrentQuestionIndex(siguienteIndex);
      } else {
        // Si no hay siguiente pregunta, mostrar finalizar
        setCurrentQuestionIndex(encuestaData.encuesta.preguntas.length);
      }
    } else {
      // Si no hay siguiente, avanzar a la siguiente pregunta en orden
      if (currentQuestionIndex < encuestaData.encuesta.preguntas.length - 1) {
        setCurrentQuestionIndex(currentQuestionIndex + 1);
      } else {
        // Última pregunta, mostrar finalizar
        setCurrentQuestionIndex(encuestaData.encuesta.preguntas.length);
      }
    }
  };

  const handleGuardarTexto = () => {
    if (!encuestaData || !esperandoTexto) {
      return;
    }

    const { preguntaIndice, opcionIndice, siguiente } = esperandoTexto;

    // Actualizar la respuesta con el texto capturado
    const respuestasActualizadas = respuestas.respuestas.map(r =>
      r.pregunta === preguntaIndice && r.respuesta === opcionIndice ? { ...r, respuesta_texto: textoRespuestas[preguntaIndice] || '' } : r,
    );
    setRespuestas({ respuestas: respuestasActualizadas });

    // Limpiar el estado de espera
    setEsperandoTexto(null);

    // Determinar la siguiente pregunta
    if (siguiente !== undefined) {
      const siguienteIndex = encuestaData.encuesta.preguntas.findIndex(p => p.indice === siguiente);
      if (siguienteIndex !== -1) {
        setCurrentQuestionIndex(siguienteIndex);
      } else {
        // Si no hay siguiente pregunta, mostrar finalizar
        setCurrentQuestionIndex(encuestaData.encuesta.preguntas.length);
      }
    } else {
      // Si no hay siguiente, avanzar a la siguiente pregunta en orden
      if (currentQuestionIndex < encuestaData.encuesta.preguntas.length - 1) {
        setCurrentQuestionIndex(currentQuestionIndex + 1);
      } else {
        // Última pregunta, mostrar finalizar
        setCurrentQuestionIndex(encuestaData.encuesta.preguntas.length);
      }
    }
  };

  const handleTextoChange = (preguntaIndice: number, texto: string) => {
    setTextoRespuestas(prev => ({
      ...prev,
      [preguntaIndice]: texto,
    }));

    // Si ya existe una respuesta para esta pregunta, actualizarla con el nuevo texto
    const respuestaExistente = respuestas.respuestas.find(r => r.pregunta === preguntaIndice);
    if (respuestaExistente) {
      const respuestasActualizadas = respuestas.respuestas.map(r => (r.pregunta === preguntaIndice ? { ...r, respuesta_texto: texto } : r));
      setRespuestas({ respuestas: respuestasActualizadas });
    }
  };

  const handleFinalizar = async () => {
    try {
      setSubmitting(true);
      await axios.put(`api/encuesta/${enlaceUnico}/finalizar`, respuestas);
      setCompleted(true);
    } catch (err: any) {
      setError('Error al finalizar la encuesta. Por favor, intente nuevamente.');
      setSubmitting(false);
    }
  };

  const getCurrentQuestion = (): IPregunta | null => {
    if (!encuestaData || currentQuestionIndex >= encuestaData.encuesta.preguntas.length) {
      return null;
    }
    return encuestaData.encuesta.preguntas[currentQuestionIndex];
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        <Spinner color="primary" />
      </div>
    );
  }

  if (error) {
    return (
      <Row>
        <Col md="8" className="mx-auto">
          <Alert color="danger">{error}</Alert>
        </Col>
      </Row>
    );
  }

  if (completed) {
    return (
      <Row>
        <Col md="8" className="mx-auto">
          <Card>
            <CardHeader>
              <h3>Encuesta Completada</h3>
            </CardHeader>
            <CardBody>
              <Alert color="success">
                <h4>¡Gracias por completar la encuesta!</h4>
                <p>Sus respuestas han sido guardadas exitosamente.</p>
              </Alert>
            </CardBody>
          </Card>
        </Col>
      </Row>
    );
  }

  if (!encuestaData) {
    return null;
  }

  const currentQuestion = getCurrentQuestion();
  const isLastQuestion = currentQuestionIndex >= encuestaData.encuesta.preguntas.length - 1;
  const showFinalizar = currentQuestionIndex >= encuestaData.encuesta.preguntas.length;

  return (
    <Row>
      <Col md="10" className="mx-auto">
        <Card>
          <CardHeader>
            <h2>{encuestaData.encuesta.titulo}</h2>
            <div className="mt-2">
              <p className="mb-1">
                <strong>Encuestado:</strong> {encuestaData.encuestado.nombre}
              </p>
              {encuestaData.encuestado.claveEmpleado && (
                <p className="mb-0">
                  <strong>Clave de Empleado:</strong> {encuestaData.encuestado.claveEmpleado}
                </p>
              )}
            </div>
          </CardHeader>
          <CardBody>
            {showFinalizar ? (
              <div>
                <h4>¿Desea finalizar la encuesta?</h4>
                <p>Por favor, revise sus respuestas antes de finalizar.</p>
                <Button color="success" onClick={handleFinalizar} disabled={submitting} className="me-2">
                  {submitting ? 'Finalizando...' : 'Finalizar Encuesta'}
                </Button>
                <Button
                  color="secondary"
                  onClick={() => setCurrentQuestionIndex(encuestaData.encuesta.preguntas.length - 1)}
                  disabled={submitting}
                >
                  Volver
                </Button>
              </div>
            ) : currentQuestion ? (
              <div>
                <div className="mb-3">
                  <h4>
                    Pregunta {currentQuestion.indice} de {encuestaData.encuesta.preguntas.length}
                  </h4>
                  <p className="lead">{currentQuestion.enunciado}</p>
                </div>
                {esperandoTexto && esperandoTexto.preguntaIndice === currentQuestion.indice ? (
                  <div>
                    <div className="mb-3">
                      <Label for={`texto-respuesta-${currentQuestion.indice}`}>
                        <strong>Por favor, proporcione más detalles:</strong>
                      </Label>
                      <Input
                        type="textarea"
                        id={`texto-respuesta-${currentQuestion.indice}`}
                        rows={4}
                        value={textoRespuestas[currentQuestion.indice] || ''}
                        onChange={e => handleTextoChange(currentQuestion.indice, e.target.value)}
                        placeholder="Escriba su respuesta aquí..."
                      />
                    </div>
                    <div className="mb-3">
                      <Button color="success" onClick={handleGuardarTexto} className="me-2">
                        Guardar
                      </Button>
                      <Button
                        color="secondary"
                        onClick={() => {
                          setEsperandoTexto(null);
                          // Restaurar el texto si había uno guardado
                          const respuestaExistente = respuestas.respuestas.find(r => r.pregunta === currentQuestion.indice);
                          if (respuestaExistente?.respuesta_texto) {
                            setTextoRespuestas(prev => ({
                              ...prev,
                              [currentQuestion.indice]: respuestaExistente.respuesta_texto,
                            }));
                          }
                        }}
                      >
                        Cancelar
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="mb-3">
                    {currentQuestion.opciones.map((opcion: IOpcion) => (
                      <Button
                        key={opcion.indice}
                        color="primary"
                        outline
                        className="d-block w-100 mb-2"
                        onClick={() => handleAnswer(currentQuestion.indice, opcion.indice, opcion.siguiente)}
                      >
                        {opcion.enunciado}
                      </Button>
                    ))}
                  </div>
                )}
                {!esperandoTexto && currentQuestionIndex > 0 && (
                  <Button
                    color="secondary"
                    onClick={() => {
                      setEsperandoTexto(null);
                      setCurrentQuestionIndex(currentQuestionIndex - 1);
                    }}
                  >
                    Anterior
                  </Button>
                )}
              </div>
            ) : null}
          </CardBody>
        </Card>
      </Col>
    </Row>
  );
};

export default EncuestaPublic;

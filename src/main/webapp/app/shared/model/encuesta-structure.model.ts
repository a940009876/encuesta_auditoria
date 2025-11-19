export interface IOpcion {
  indice: number;
  enunciado: string;
  siguiente?: number;
  abierta?: number;
}

export interface IPregunta {
  indice: number;
  enunciado: string;
  opciones: IOpcion[];
  abierta?: number;
}

export interface IEncuestaStructure {
  titulo: string;
  preguntas: IPregunta[];
}

export interface IRespuesta {
  pregunta: number;
  respuesta: number;
  respuesta_texto?: string;
}

export interface IRespuestasEncuesta {
  respuestas: IRespuesta[];
}

export interface IEncuestaData {
  aplicacionEncuestaId: number;
  encuestado: {
    id: number;
    nombre: string;
    fechaNacimiento: string;
    claveEmpleado: string;
  };
  encuesta: IEncuestaStructure;
}

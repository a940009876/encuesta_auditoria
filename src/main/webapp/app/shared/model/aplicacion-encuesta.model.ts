import dayjs from 'dayjs';
import { IEncuestado } from 'app/shared/model/encuestado.model';
import { IEncuesta } from 'app/shared/model/encuesta.model';

export interface IAplicacionEncuesta {
  id?: number;
  enlaceUnico?: string | null;
  fechaAplicacion?: dayjs.Dayjs | null;
  respuestaEncuesta?: string | null;
  encuestado?: IEncuestado | null;
  encuesta?: IEncuesta | null;
}

export const defaultValue: Readonly<IAplicacionEncuesta> = {};

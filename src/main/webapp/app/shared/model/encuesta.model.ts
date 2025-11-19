import dayjs from 'dayjs';

export interface IEncuesta {
  id?: number;
  nombre?: string | null;
  vigenciaDesde?: dayjs.Dayjs | null;
  vigenciaHasta?: dayjs.Dayjs | null;
  structuraJson?: string | null;
}

export const defaultValue: Readonly<IEncuesta> = {};

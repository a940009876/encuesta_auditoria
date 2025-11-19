import dayjs from 'dayjs';

export interface IEncuestado {
  id?: number;
  nombre?: string | null;
  fechaNacimiento?: dayjs.Dayjs | null;
  claveEmpleado?: string | null;
}

export const defaultValue: Readonly<IEncuestado> = {};

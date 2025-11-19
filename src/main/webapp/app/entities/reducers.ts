import encuestado from 'app/entities/encuestado/encuestado.reducer';
import encuesta from 'app/entities/encuesta/encuesta.reducer';
import aplicacionEncuesta from 'app/entities/aplicacion-encuesta/aplicacion-encuesta.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  encuestado,
  encuesta,
  aplicacionEncuesta,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;

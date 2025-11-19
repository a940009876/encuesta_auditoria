import React from 'react';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/encuestado">
        Encuestado
      </MenuItem>
      <MenuItem icon="asterisk" to="/encuesta">
        Encuesta
      </MenuItem>
      <MenuItem icon="asterisk" to="/aplicacion-encuesta">
        Aplicacion Encuesta
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;

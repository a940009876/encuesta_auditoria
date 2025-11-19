package mx.com.ado.auditoria.domain;

import static mx.com.ado.auditoria.domain.AplicacionEncuestaTestSamples.*;
import static mx.com.ado.auditoria.domain.EncuestaTestSamples.*;
import static mx.com.ado.auditoria.domain.EncuestadoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import mx.com.ado.auditoria.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AplicacionEncuestaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AplicacionEncuesta.class);
        AplicacionEncuesta aplicacionEncuesta1 = getAplicacionEncuestaSample1();
        AplicacionEncuesta aplicacionEncuesta2 = new AplicacionEncuesta();
        assertThat(aplicacionEncuesta1).isNotEqualTo(aplicacionEncuesta2);

        aplicacionEncuesta2.setId(aplicacionEncuesta1.getId());
        assertThat(aplicacionEncuesta1).isEqualTo(aplicacionEncuesta2);

        aplicacionEncuesta2 = getAplicacionEncuestaSample2();
        assertThat(aplicacionEncuesta1).isNotEqualTo(aplicacionEncuesta2);
    }

    @Test
    void encuestadoTest() {
        AplicacionEncuesta aplicacionEncuesta = getAplicacionEncuestaRandomSampleGenerator();
        Encuestado encuestadoBack = getEncuestadoRandomSampleGenerator();

        aplicacionEncuesta.setEncuestado(encuestadoBack);
        assertThat(aplicacionEncuesta.getEncuestado()).isEqualTo(encuestadoBack);

        aplicacionEncuesta.encuestado(null);
        assertThat(aplicacionEncuesta.getEncuestado()).isNull();
    }

    @Test
    void encuestaTest() {
        AplicacionEncuesta aplicacionEncuesta = getAplicacionEncuestaRandomSampleGenerator();
        Encuesta encuestaBack = getEncuestaRandomSampleGenerator();

        aplicacionEncuesta.setEncuesta(encuestaBack);
        assertThat(aplicacionEncuesta.getEncuesta()).isEqualTo(encuestaBack);

        aplicacionEncuesta.encuesta(null);
        assertThat(aplicacionEncuesta.getEncuesta()).isNull();
    }
}

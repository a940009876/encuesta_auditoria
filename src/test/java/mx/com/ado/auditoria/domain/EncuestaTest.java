package mx.com.ado.auditoria.domain;

import static mx.com.ado.auditoria.domain.AplicacionEncuestaTestSamples.*;
import static mx.com.ado.auditoria.domain.EncuestaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import mx.com.ado.auditoria.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EncuestaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Encuesta.class);
        Encuesta encuesta1 = getEncuestaSample1();
        Encuesta encuesta2 = new Encuesta();
        assertThat(encuesta1).isNotEqualTo(encuesta2);

        encuesta2.setId(encuesta1.getId());
        assertThat(encuesta1).isEqualTo(encuesta2);

        encuesta2 = getEncuestaSample2();
        assertThat(encuesta1).isNotEqualTo(encuesta2);
    }

    @Test
    void aplicacionEncuestaTest() {
        Encuesta encuesta = getEncuestaRandomSampleGenerator();
        AplicacionEncuesta aplicacionEncuestaBack = getAplicacionEncuestaRandomSampleGenerator();

        encuesta.addAplicacionEncuesta(aplicacionEncuestaBack);
        assertThat(encuesta.getAplicacionEncuestas()).containsOnly(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuesta()).isEqualTo(encuesta);

        encuesta.removeAplicacionEncuesta(aplicacionEncuestaBack);
        assertThat(encuesta.getAplicacionEncuestas()).doesNotContain(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuesta()).isNull();

        encuesta.aplicacionEncuestas(new HashSet<>(Set.of(aplicacionEncuestaBack)));
        assertThat(encuesta.getAplicacionEncuestas()).containsOnly(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuesta()).isEqualTo(encuesta);

        encuesta.setAplicacionEncuestas(new HashSet<>());
        assertThat(encuesta.getAplicacionEncuestas()).doesNotContain(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuesta()).isNull();
    }
}

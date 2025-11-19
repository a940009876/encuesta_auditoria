package mx.com.ado.auditoria.domain;

import static mx.com.ado.auditoria.domain.AplicacionEncuestaTestSamples.*;
import static mx.com.ado.auditoria.domain.EncuestadoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import mx.com.ado.auditoria.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EncuestadoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Encuestado.class);
        Encuestado encuestado1 = getEncuestadoSample1();
        Encuestado encuestado2 = new Encuestado();
        assertThat(encuestado1).isNotEqualTo(encuestado2);

        encuestado2.setId(encuestado1.getId());
        assertThat(encuestado1).isEqualTo(encuestado2);

        encuestado2 = getEncuestadoSample2();
        assertThat(encuestado1).isNotEqualTo(encuestado2);
    }

    @Test
    void aplicacionEncuestaTest() {
        Encuestado encuestado = getEncuestadoRandomSampleGenerator();
        AplicacionEncuesta aplicacionEncuestaBack = getAplicacionEncuestaRandomSampleGenerator();

        encuestado.addAplicacionEncuesta(aplicacionEncuestaBack);
        assertThat(encuestado.getAplicacionEncuestas()).containsOnly(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuestado()).isEqualTo(encuestado);

        encuestado.removeAplicacionEncuesta(aplicacionEncuestaBack);
        assertThat(encuestado.getAplicacionEncuestas()).doesNotContain(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuestado()).isNull();

        encuestado.aplicacionEncuestas(new HashSet<>(Set.of(aplicacionEncuestaBack)));
        assertThat(encuestado.getAplicacionEncuestas()).containsOnly(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuestado()).isEqualTo(encuestado);

        encuestado.setAplicacionEncuestas(new HashSet<>());
        assertThat(encuestado.getAplicacionEncuestas()).doesNotContain(aplicacionEncuestaBack);
        assertThat(aplicacionEncuestaBack.getEncuestado()).isNull();
    }
}

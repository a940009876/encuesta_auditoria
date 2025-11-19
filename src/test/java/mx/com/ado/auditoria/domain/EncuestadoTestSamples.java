package mx.com.ado.auditoria.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EncuestadoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Encuestado getEncuestadoSample1() {
        return new Encuestado().id(1L).nombre("nombre1").claveEmpleado("claveEmpleado1");
    }

    public static Encuestado getEncuestadoSample2() {
        return new Encuestado().id(2L).nombre("nombre2").claveEmpleado("claveEmpleado2");
    }

    public static Encuestado getEncuestadoRandomSampleGenerator() {
        return new Encuestado()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .claveEmpleado(UUID.randomUUID().toString());
    }
}

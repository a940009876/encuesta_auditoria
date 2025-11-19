package mx.com.ado.auditoria.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AplicacionEncuestaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AplicacionEncuesta getAplicacionEncuestaSample1() {
        return new AplicacionEncuesta().id(1L).enlaceUnico("enlaceUnico1");
    }

    public static AplicacionEncuesta getAplicacionEncuestaSample2() {
        return new AplicacionEncuesta().id(2L).enlaceUnico("enlaceUnico2");
    }

    public static AplicacionEncuesta getAplicacionEncuestaRandomSampleGenerator() {
        return new AplicacionEncuesta().id(longCount.incrementAndGet()).enlaceUnico(UUID.randomUUID().toString());
    }
}

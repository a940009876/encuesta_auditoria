package mx.com.ado.auditoria.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EncuestaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Encuesta getEncuestaSample1() {
        return new Encuesta().id(1L).nombre("nombre1");
    }

    public static Encuesta getEncuestaSample2() {
        return new Encuesta().id(2L).nombre("nombre2");
    }

    public static Encuesta getEncuestaRandomSampleGenerator() {
        return new Encuesta().id(longCount.incrementAndGet()).nombre(UUID.randomUUID().toString());
    }
}

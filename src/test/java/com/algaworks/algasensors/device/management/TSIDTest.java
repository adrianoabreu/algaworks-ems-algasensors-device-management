package com.algaworks.algasensors.device.management;

import io.hypersistence.tsid.TSID;
//import org.junit.jupiter.api.Assertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

class TSIDTest {

    @Test
    void shouldGenerateTSID(){
//        System.setProperty("tsid.node", "2");
//        System.setProperty("tsid.node.count", "32");

//        TSID.Factory factory = TSID.Factory.builder().build();
//        TSID tsid = factory.generate();

        TSID tsid = IdGeneration.generateTSID();
        Assertions.assertThat(tsid.getInstant())
                .isCloseTo(Instant.now(), Assertions.within(1, ChronoUnit.MINUTES));

//        TSID tsid = TSID.Factory.getTsid();
        System.out.println(tsid);
        System.out.println(tsid.toLong());
        System.out.println(tsid.getInstant());
    }
}

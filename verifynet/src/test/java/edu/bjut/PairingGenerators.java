package edu.bjut;

import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PairingGenerators {

    @Test
    public void testGen() {
        int rBits = 160;
        int qBits = 512;

        // JPBC Type A pairing generator...
        PairingParametersGenerator<?> pg = new TypeACurveGenerator(rBits, qBits);

        // Then, generate the parameters by invoking the generate method.
        PairingParameters params = pg.generate();
        Pairing pairing = PairingFactory.getPairing(params);
        assertNotNull(pairing);
    }

    @Test
    public void testGenParamsFromProperties() {
        Pairing pairing = PairingFactory.getPairing("params/mm/ctl13/toy.properties");
        assertNotNull(pairing);
    }
    
}

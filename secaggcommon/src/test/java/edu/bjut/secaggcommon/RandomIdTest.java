package edu.bjut.secaggcommon;

import org.junit.Test;

import edu.bjut.common.util.Utils;

public class RandomIdTest {
    
    @Test
    public void randomIdString() {
        for (int i = 0; i < 1; ++i) {
            long id = Utils.randomlong();
            System.out.println(String.valueOf(id));
            System.out.println(String.valueOf(id).length());
        }

        
    }
}

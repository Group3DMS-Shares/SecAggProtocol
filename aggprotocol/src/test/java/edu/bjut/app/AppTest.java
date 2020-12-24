package edu.bjut.app;

import java.net.URL;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        System.out.println(url);
    }
}

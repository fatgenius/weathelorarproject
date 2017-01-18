package com.SaxionWeatherStation;

import static org.junit.Assert.*;


/**
 * Unit test for simple App.
 */
public class AppTest {


    @org.junit.Test
    public void testIsFloat() {
        String floatString = "11.433";
        String word = "hello";
        assertTrue(App.isFloat(floatString));
        assertFalse(App.isFloat(word));
    }

    @org.junit.Test
    public void testIsInteger() {
        String integerString = "25";
        String floatString = "11.433";
        String word = "hello";
        assertTrue(App.isInteger(integerString));
        assertFalse(App.isInteger(floatString));
        assertFalse(App.isInteger(word));
    }

    @org.junit.Test
    public void testParseMessage() {
        String invalidDataFormat = "hello world!";
        String temperature = "t-10";
        String humidity = "h37.9";
        String msg = App.parseMessage(invalidDataFormat);
        String msg2 = App.parseMessage(temperature);
        String msg3 = App.parseMessage(humidity);
        assertEquals("invalid string format", msg);
        assertEquals("-10", msg2);
        assertEquals("37.9", msg3);
    }
}

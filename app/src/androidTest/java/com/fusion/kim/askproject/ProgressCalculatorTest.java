package com.fusion.kim.askproject;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ProgressCalculatorTest {
    @Test
    public void testConvertFahrenheitToCelsius() {
        double actual = MainActivity.calculateProgress(2, 5);
        // expected value is 40
        double expected = 40;

        assertEquals("Progress calculation failed", expected, actual, 0.001);
    }
}
package com.lazyfoxv.able;

import com.lazyfoxv.able.util.HexUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void hexTest1() {
        String str = HexUtil.binStrToHexStr("00000011");
        System.out.println(str);
    }
    @Test
    public void hexTest2() {
        String str = HexUtil.deciToHexStrOfOneByte(15);
        System.out.println(str);
    }
}
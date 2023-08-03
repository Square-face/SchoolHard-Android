package com.example.schoolhard

import com.example.schoolhard.utils.getDelta
import com.example.schoolhard.utils.getDeltaString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LessonTimeUnitTest {
    @Test
    fun timeDelta() {
        val t1 = Date(0)
        val t2 = Date(100)
        assertEquals(100, getDelta(t1, t2))

        val t3 = Date(500)
        assertEquals(400, getDelta(t2, t3))
        assertEquals(-400, getDelta(t3, t2))
    }

    @Test
    fun deltaToString() {
        assertEquals("4 min",               getDeltaString(60*1000*4))
        assertEquals("23 h",                getDeltaString(60*60*1000*23))
        assertEquals("2 days",              getDeltaString(24*60*60*1000*2))
        assertEquals("7 h 43 min",          getDeltaString(60*60*1000*7 + 60*1000*43))
        assertEquals("1 days 27 min",       getDeltaString(24*60*60*1000*1 + 60*1000*27))
        assertEquals("1 days 3 h 9 min",    getDeltaString(24*60*60*1000*1 + 60*60*1000*3 + 60*1000*9))
        assertEquals("2 days",              getDeltaString(24*60*60*1000*2 + 60*60*1000*10 + 60*1000*1))
        assertEquals("5 days",              getDeltaString(24*60*60*1000*5 + 60*60*1000*12 + 60*1000*15))
        assertEquals("now!",                getDeltaString(0))
        assertEquals("60 sec",               getDeltaString(60*1000))
        assertEquals("3 sec",                getDeltaString(1000*3))
        assertEquals("59 sec",                getDeltaString(1000*59))
    }
}
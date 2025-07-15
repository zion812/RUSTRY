package com.rio.rustry

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rio.rustry", appContext.packageName)
    }

    @Test
    fun testBasicInstrumentation() {
        // Test that instrumentation is working
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assertNotNull("Instrumentation should not be null", instrumentation)
        
        val context = instrumentation.context
        assertNotNull("Instrumentation context should not be null", context)
        
        val targetContext = instrumentation.targetContext
        assertNotNull("Target context should not be null", targetContext)
        assertEquals("Package name should match", "com.rio.rustry", targetContext.packageName)
    }

    @Test
    fun testApplicationInfo() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val applicationInfo = appContext.applicationInfo
        
        assertNotNull("Application info should not be null", applicationInfo)
        assertTrue("Application should be debuggable in test", 
            (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    }
}
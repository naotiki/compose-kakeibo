package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.ui.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("xyz.naotiki_apps.compose_kakeibo", appContext.packageName)


        /* val db=AppDatabase.getDatabase(appContext)
        db.categoryDao().insertAll(Category(name = "肉", color = Color.Red))*/
    }
}

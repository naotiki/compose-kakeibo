package xyz.naotiki_apps.compose_kakeibo

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.compose.material.Colors
import androidx.compose.ui.graphics.toArgb
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config


@Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
class RobolectricTest {
    @Test
    fun read() {

        val appContext = ApplicationProvider.getApplicationContext<Context>()
        println("END!!!!")


    }
}

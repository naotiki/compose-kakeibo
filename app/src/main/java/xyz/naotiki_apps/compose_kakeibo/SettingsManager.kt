package xyz.naotiki_apps.compose_kakeibo

import android.content.SharedPreferences
import javax.inject.Inject

class SettingsManager private constructor() {
    @Inject lateinit var sharedPreferences: SharedPreferences

    init {

    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(): SettingsManager = INSTANCE?: synchronized(this){
            SettingsManager()
        }
    }
}
object OmitYen : Settings.BooleanSettings("omit_yen", false)

object ExportPath : Settings.StringSettings("export_path", null)

object Splitter: Settings.EnumSettings<DateStringFormatter.FormatType>("splitter", DateStringFormatter.FormatType.Char){
    override fun onUpdated(newValue: Int) {

    }
}

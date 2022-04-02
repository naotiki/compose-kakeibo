package xyz.naotiki_apps.compose_kakeibo

import android.content.SharedPreferences

class SettingsManager private constructor(sharedPreferences: SharedPreferences) {
    private val settings = arrayOf(OmitYen,ExportPath,Splitter)
    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { preferences, key ->
            settings.first { it.key==key }.read(preferences)
        }
        //When call read(),Settings.state.value initialize to the latest value
        settings.forEach {
            it.read(sharedPreferences)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun init(sharedPreferences: SharedPreferences): SettingsManager {
            INSTANCE= SettingsManager(sharedPreferences)
            return INSTANCE!!
        }
        fun getInstance(): SettingsManager = INSTANCE!!
    }
}
object OmitYen : Settings.BooleanSettings("omit_yen", false)

object ExportPath : Settings.StringSettings("export_path", null)

object Splitter: Settings.EnumSettings<DateStringFormatter.FormatType>("splitter", DateStringFormatter.FormatType.Char)

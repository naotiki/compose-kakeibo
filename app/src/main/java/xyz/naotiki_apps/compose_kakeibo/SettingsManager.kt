package xyz.naotiki_apps.compose_kakeibo

import android.content.SharedPreferences
import javax.inject.Inject

class SettingsManager private constructor() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    init {

    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(): SettingsManager = INSTANCE ?: synchronized(this) {
            SettingsManager()
        }
    }
}

object Settings {
    val OmitYen = SettingItem.BooleanSettingItem("omit_yen", false)

    val ExportPath = SettingItem.StringSettingItem("export_path", null)

    val Splitter = SettingItem.EnumSettingItem("splitter", DateStringFormatter.FormatType.Char)


    private val settings = arrayOf(OmitYen, ExportPath, Splitter)
    fun init(sharedPreferences: SharedPreferences) {
        sharedPreferences.registerOnSharedPreferenceChangeListener { preferences, key ->
            settings.first { it.key == key }.read()
        }
        //When call read(),Settings.state.value initialize to the latest value
        settings.forEach {
            it.init(sharedPreferences)
            it.read()
        }
    }
}

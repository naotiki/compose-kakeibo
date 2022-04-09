package xyz.naotiki_apps.compose_kakeibo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.naotiki_apps.compose_kakeibo.EnumExtensions.Companion.toEnum
import xyz.naotiki_apps.compose_kakeibo.Settings.ExportPath
import xyz.naotiki_apps.compose_kakeibo.Settings.OmitYen
import xyz.naotiki_apps.compose_kakeibo.Settings.Splitter

import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext application: Context,
    private val preferences: SharedPreferences,
) : AndroidViewModel(application as Application) {


    fun setExportPath(uri: Uri?) {
        if (uri != null) {
            val contentResolver = getApplication<KakeiboApplication>().contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            ExportPath.write( uri.path)
        }
    }

    fun <T> writeSetting(s: SettingItem<T>, value: T) = s.write( value)

    fun <T> readSettings(s: SettingItem<T>): T {
        s.read()
        return s.state.value
    }
}

abstract class SettingItem<T>(val key: String, val defaultValue: T) {
    protected lateinit var sharedPreferences: SharedPreferences
    fun init(sharedPreferences: SharedPreferences){
        this.sharedPreferences=sharedPreferences
    }
    var state = mutableStateOf(defaultValue)
    protected abstract fun readFromPreferences(): T

    fun read() {
        state.value = readFromPreferences()
    }

    fun write( value: T){
        writeToPreferences( value)
    }
    protected abstract fun writeToPreferences( value: T)


    open class EnumSettingItem<T : Enum<T>>(key: String, defaultValue: T) : SettingItem<Int>(key, defaultValue.ordinal) {
        override fun readFromPreferences(): Int {
            return sharedPreferences.getInt(key, defaultValue)
        }

        inline fun <reified T : Enum<T>> castedValue(): T? {
            return state.value.toEnum<T>()
        }


        override fun writeToPreferences( value: Int) {
            with(sharedPreferences.edit()){
                putInt(key,value)
                apply()
            }
        }


    }

    open class StringSettingItem(key: String, defaultValue: String?) : SettingItem<String?>(key, defaultValue) {
        override fun readFromPreferences(): String? {
            return sharedPreferences.getString(key, defaultValue)
        }

        override fun writeToPreferences( value: String?) {
            with(sharedPreferences.edit()) {
                putString(key, value)
                apply()
            }
        }



    }

    open class BooleanSettingItem(key: String, defaultValue: Boolean) : SettingItem<Boolean>(key, defaultValue) {
        override fun readFromPreferences(): Boolean {
            return sharedPreferences.getBoolean(key, defaultValue)
        }

        override fun writeToPreferences( value: Boolean) {
            with(sharedPreferences.edit()) {
                putBoolean(key, value)
                apply()
            }
        }
    }
}


@Preview
@Composable
fun SettingsBody(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    Scaffold(topBar = {
        TopAppBar({ Text("設定") }, navigationIcon = {
            IconButton({

            }) {
                Icon(Icons.Default.ArrowBack, null)
            }
        })

    }) { paddingValues: PaddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues), Arrangement.spacedBy(5.dp)) {
            val exportPath by remember { ExportPath.state }
            val l = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocumentTree(),
                settingsViewModel::setExportPath
            )
            SettingElement("ファイル保存場所", exportPath ?: "設定されていません") {
                Button({
                    val path = exportPath?.let {
                        File(it).toUri()
                    }
                    l.launch(path)
                }) {
                    Text("保存場所を変更")
                }
            }

            val enable by remember { OmitYen.state }
            SettingElement("「円」を省略", "音声認識時の「円」を言わなくてもいいようにします。") {
                Switch(enable, onCheckedChange = { settingsViewModel.writeSetting(OmitYen, it) })
            }
            val spliter by remember { Splitter.state }
            var expanded by remember { mutableStateOf(false) }
            SettingElement("日付形式","日付の表示形式を選択できます。"){
                IconButton({expanded=true}){
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,null)
                    DropdownMenu(expanded,{expanded=false}){
                        DropdownMenuItem({}){
                            Text("")
                        }
                    }
                }
            }



            Divider()
            SettingElement("ライセンス", "") {
                IconButton({
                    TODO("Navigate to License Screen")
                }) {
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }
    }
}

@Composable
fun SettingElement(
    name: String,
    description: String,
    changePart: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.width(5.dp))
        Column(Modifier.weight(1f), Arrangement.SpaceEvenly) {
            Text(name, style = MaterialTheme.typography.h6)
            Text(description, style = MaterialTheme.typography.caption)
        }
        Spacer(Modifier.width(5.dp))
        changePart()
    }
}


@Preview
@Composable
fun Pre1() {
    Column {

    }
}

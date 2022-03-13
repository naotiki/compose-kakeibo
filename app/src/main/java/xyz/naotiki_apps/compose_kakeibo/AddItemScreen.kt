package xyz.naotiki_apps.compose_kakeibo

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    @ApplicationContext application: Context,
    private val itemDataRepository: ItemDataRepository
) : AndroidViewModel(application as Application) {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(application).also {
        it.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                speechSoundLevel = 0F
                speechState = SpeechState.Ready
            }

            override fun onBeginningOfSpeech() {
                speechState = SpeechState.Listening
            }

            override fun onError(errorCode: Int) {
                speechSoundLevel = 0F
                speechState = SpeechState.Error
                Toast.makeText(application, errorCodeToMessage(errorCode), Toast.LENGTH_LONG).show()
            }

            override fun onResults(results: Bundle) {
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData != null) {
                    var speechResult = recData[0]
                    speechResult = speechResult.replace("万", "0000")


                    hearText = speechResult
                    speechState = SpeechState.Done
                } else {
                    speechState = SpeechState.Error
                }
                speechSoundLevel = 0F
            }

            override fun onPartialResults(results: Bundle) {
                speechState = SpeechState.Knowing
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData != null) {
                    hearText = recData[0]
                }
            }

            override fun onRmsChanged(p0: Float) {
                speechSoundLevel = p0
                Log.i("SoundLevel", p0.toString())
            }

            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {
                speechSoundLevel = 0F
            }

            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }
    private val regex = "[0-9]+[円]".toRegex()

    enum class SpeechState(val message: String?) {
        None(""),
        Ready("お話しください"),
        Listening("認識中"),

        //途中認識
        Knowing(null),
        Done(null),
        Error("エラーが発生しました")
    }

    var speechSoundLevel by mutableStateOf(0F)
    var speechState by mutableStateOf(SpeechState.None)
    var hearText by mutableStateOf<String?>(null)

    fun parseHearText(): Pair<String, Int?> =
        hearText!!.replace(regex, "") to regex.find(hearText!!)?.value?.replace("万", "0000")?.replaceFirst("円", "")
            ?.toIntOrNull()

    fun startSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        speechRecognizer.startListening(intent)
    }

    fun cancelSpeech() {
        speechRecognizer.cancel()
        speechState = SpeechState.None
        speechSoundLevel = 0F
        Toast.makeText(getApplication(), "キャンセルされました", Toast.LENGTH_LONG).show()
    }


    private fun errorCodeToMessage(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "オーディオエラー"
        SpeechRecognizer.ERROR_CLIENT -> "クライアントエラー"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "パーミッションエラー"
        SpeechRecognizer.ERROR_NETWORK -> "ネットワークエラー"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ネットワークタイムアウトエラー"
        SpeechRecognizer.ERROR_NO_MATCH -> "認識できませんでした"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "再度お試しください"
        SpeechRecognizer.ERROR_SERVER -> "サーバーエラー"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "認識できませんでした"
        /*SpeechRecognizer.ERROR_TOO_MANY_REQUESTS,
        SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE->*/
        else -> "不明なエラー"
    }

    fun addItemData(date: Date, name: String, price: String, categoryId: Int) {
        if (name.isNotBlank() && price.isNotBlank() && price.isDigitsOnly()) {
            itemDataRepository.insertItemData(ItemData(date = date, name = name, price = price.toInt(), categoryId = categoryId))
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)

@Composable
fun AddItemBody(
    date: Date,
    toNavigateFunc: NavigateFunc,
    viewModel: AddItemViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val micPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    val canStart =
        viewModel.speechState == AddItemViewModel.SpeechState.None || viewModel.speechState == AddItemViewModel.SpeechState.Error
    var helpDialogOpen by remember { mutableStateOf(false) }
    if (helpDialogOpen) {
        AlertDialog({
            helpDialogOpen = false
        }, title = {
            Text("音声入力のヘルプ")
        }, text = {
            Column {
                Text("「水100円」のように品名と値段を続けて言ってください。")
                Text("値段の後には必ず「円」と言いましょう。")
                Text("商品名の末尾が数字の場合は値段に誤認識される場合があります。")
            }

        }, confirmButton = {
            TextButton({ helpDialogOpen = false }) {
                Text("OK")
            }
        })
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text("データを追加")
        }, navigationIcon = {
            IconButton({ toNavigateFunc(KakeiboScreen.NAVIGATE_TO_BACK) }) {
                Icon(Icons.Default.ArrowBack, null)
            }
        })
    }) {
        val fragmentManager = (LocalContext.current as AppCompatActivity).supportFragmentManager
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    date.toString(),
                    modifier = Modifier.clickable {
                        val datePicker =
                            MaterialDatePicker.Builder.datePicker().setSelection(date.toMilliLong()).build()
                        datePicker.addOnPositiveButtonClickListener {
                            toNavigateFunc(KakeiboScreen.AddItem.name + "/${Date.dateFromLong(it).toInt()}")
                        }
                        datePicker.show(fragmentManager, "a")
                    },
                    fontSize = 25.sp,
                    style = TextStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                )
                Text("にデータを追加")
            }
            OutlinedTextField(name, onValueChange = { name = it }, label = { Text("品名") }, singleLine = true)
            OutlinedTextField(
                price,
                onValueChange = {
                    if (it.isDigitsOnly()) {
                        price = it.trimStart { it == '0' }
                    }
                },
                label = { Text("値段") },
                trailingIcon = { Text("円", color = MaterialTheme.colors.onSurface) },
                placeholder = { Text("整数のみ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row {
                when (micPermissionState.status) {
                    PermissionStatus.Granted -> {
                        Button(
                            {
                                if (canStart) {
                                    viewModel.startSpeech()
                                } else {
                                    viewModel.cancelSpeech()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = if (canStart) MaterialTheme.colors.primary else Color.LightGray)
                        ) {
                            if (canStart) {

                                Icon(Icons.Default.Mic, null)
                                Text("音声入力")
                            } else {
                                Icon(Icons.Default.Cancel, null)
                                Text("キャンセル")
                            }

                        }
                        IconButton({
                            helpDialogOpen = true
                        }) {
                            Icon(Icons.Default.Help, null)
                        }
                    }
                    is PermissionStatus.Denied -> {
                        Text("音声入力を使うには権限を許可してください")
                        Button({
                            micPermissionState.launchPermissionRequest()
                        }) {
                            Text("許可")
                        }
                    }
                }
            }
            Text(viewModel.speechState.message ?: viewModel.hearText!!)
            if (viewModel.speechState == AddItemViewModel.SpeechState.Done) {
                val (resultName, resultPrice) = viewModel.parseHearText()
                name = resultName
                price = (resultPrice ?: "").toString()
                viewModel.speechState = AddItemViewModel.SpeechState.None
            }
            if (!canStart) {
                LinearProgressIndicator()
            } else {
                Divider()
            }
            /* Speech Debug
             Text(viewModel.speechSoundLevel.toString())
             Box(Modifier.background(Color.Red, CircleShape).animateContentSize().size((viewModel.speechSoundLevel*2+50).dp))
             */
            val allCategory by mainViewModel.allCategories.collectAsState(null)
            var expanded by remember { mutableStateOf(false) }
            var selectedCategoryId: Int by remember { mutableStateOf(1) }
            Column {
                TextField(
                    value =  allCategory?.first { it.id == selectedCategoryId }?.name?:"" ,
                    readOnly = true,
                    onValueChange = { },
                    label = { Text("Label") },
                    trailingIcon = {
                        IconButton({ expanded = !expanded }) {
                            Icon(if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null)
                        }
                    },

                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                if (allCategory != null) {
                    Row {
                        val categoriesWithChildren = mainViewModel.getAllCategoriesWithChildren(allCategory!!)
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            categoriesWithChildren.forEach { (parent, children) ->
                                val hasChildren = children != null
                                if (hasChildren) {
                                    Text(parent.name, Modifier.padding(5.dp))
                                    Divider()
                                    children!!.forEach {
                                        DropdownMenuItem({
                                            selectedCategoryId = it.id
                                            expanded = false
                                        }) { Spacer(Modifier.width(10.dp)); Text(it.name) }
                                    }
                                    Divider()

                                } else {
                                    DropdownMenuItem({
                                        selectedCategoryId = parent.id
                                        expanded = false
                                    }) {
                                        Text(parent.name)
                                    }
                                }

                            }

                        }
                    }
                }
            }
            val canSubmit = name.isNotBlank() && price.isNotBlank() && price.isDigitsOnly()
            Button({
                viewModel.addItemData(date,name, price, selectedCategoryId)
            }, enabled = canSubmit) {
                Icon(Icons.Default.Add, null)
                Text("データを追加")
            }
            if (!canSubmit) {
                Text("名前が空白または価格が数字ではありません。")
            }
        }
    }

}



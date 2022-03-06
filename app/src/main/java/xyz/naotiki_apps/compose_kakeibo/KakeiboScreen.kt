package xyz.naotiki_apps.compose_kakeibo

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

enum class KakeiboScreen(val body: @Composable () -> Unit) {
    Summary({}),
    AddItem({ AddItemBody() })
}
class AddItemViewModel(application: Application) : AndroidViewModel(application) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).also {
        it.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                speechState = SpeechState.Ready
            }
            override fun onBeginningOfSpeech() {
                speechState = SpeechState.Listening
            }
            override fun onError(errorCode: Int) {
                speechState = SpeechState.Error
                Toast.makeText(getApplication(), errorCodeToMessage(errorCode), Toast.LENGTH_LONG).show()
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
            }
            override fun onPartialResults(results: Bundle) {
                speechState = SpeechState.Knowing
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData != null) {
                    hearText = recData[0]
                }
            }
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
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
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun AddItemBody(viewModel: AddItemViewModel = viewModel()) {
    val micPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
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
    Column {
        TextField(name, onValueChange = { name = it }, label = { Text("品名") }, singleLine = true)
        TextField(
            price,
            onValueChange = { price = it },
            label = { Text("値段") },
            trailingIcon = { Text("円", color = MaterialTheme.colors.onSurface) },
            placeholder = { Text("日本円/JPY") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row {
            when (micPermissionState.status) {
                PermissionStatus.Granted -> {

                    Button({
                        if (canStart) {
                            viewModel.startSpeech()
                        } else {
                            viewModel.cancelSpeech()
                        }

                    }) {
                        if (canStart) {

                            Icon(Icons.Default.Mic, null)
                            Text("音声認識")
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
                    Text("音声認識を使うには権限を許可してください")
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
        var menuOpen by remember { mutableStateOf(false) }
        DropdownMenu(expanded = menuOpen, { menuOpen = false }) {

        }
    }
}



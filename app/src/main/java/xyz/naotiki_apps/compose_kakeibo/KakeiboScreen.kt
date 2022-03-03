package xyz.naotiki_apps.compose_kakeibo

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

            override fun onRmsChanged(p0: Float) {}

            override fun onBufferReceived(p0: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(errorCode: Int) {
                errorCodeToMessage(errorCode)
            }

            override fun onResults(results: Bundle) {
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData != null) {
                    val regex = "[0-9]+[円]".toRegex()
                    var speechResult = recData[0]
                    speechResult = speechResult.replace("万", "0000")

                    speechResult.replace(regex, "") to regex.find(speechResult)?.value?.replaceFirst("円", "")
                    hearText=speechResult
                    speechState = SpeechState.Done
                } else {
                    speechState = SpeechState.Error
                }
            }

            override fun onPartialResults(results: Bundle) {
                speechState = SpeechState.Knowing
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData != null) {
                    val regex = "[0-9]+[円]".toRegex()
                    var speechResult = recData[0]
                    speechResult = speechResult.replace("万", "0000")
                    hearText = speechResult
                }
            }

            override fun onEvent(p0: Int, p1: Bundle?) {}

        })
    }


    enum class SpeechState(val message: String) {
        None(""),
        Canceled("キャンセルされました"),
        Ready("お話しください"),
        Listening("認識中"),

        //途中認識
        Knowing(""),
        Done(""),
        Error("エラーが発生しました")
    }

    var speechState by mutableStateOf(SpeechState.None)
    var hearText by mutableStateOf<String?>(null)
    fun startSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        speechRecognizer.startListening(intent)
    }

    fun cancelSpeech() {
        speechRecognizer.cancel()
    }

    private fun errorCodeToMessage(errorCode: Int): String {
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> return "オーディオエラー"
            SpeechRecognizer.ERROR_CLIENT -> return "クライアントエラー"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> return "パーミッションエラー"
            SpeechRecognizer.ERROR_NETWORK -> return "ネットワークエラー"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> return "ネットワークタイムアウトエラー"
            SpeechRecognizer.ERROR_NO_MATCH -> return "認識できませんでした"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> return "再度お試しください"
            SpeechRecognizer.ERROR_SERVER -> return "サーバーエラー"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> return "認識できませんでした"
        }
        return "Unknown error"
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun AddItemBody(viewModel: AddItemViewModel = viewModel()) {
    // Camera permission state
    val micPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    Column {
        Row {
            when (micPermissionState.status) {
                PermissionStatus.Granted -> {
                    Text("音声入力", style = MaterialTheme.typography.h4)
                    IconButton({}, modifier = Modifier.background(Color.Red, CircleShape)) {
                        Icon(Icons.Default.Mic, null)
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
        Text("品名")
        TextField(name, onValueChange = { name = it })
        Text("値段")
        TextField(price, onValueChange = { price = it })
    }
}

package xyz.naotiki_apps.compose_kakeibo

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
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
    private val itemDataRepository: ItemDataRepository,
    private val categoryRepository: CategoryRepository
) : AndroidViewModel(application as Application) {
    val allCategories=categoryRepository.getAllCategories().asLiveData().distinctUntilChanged()
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
                val scores=results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (recData != null&&scores!=null) {

                   var marged= mutableMapOf(*recData.mapIndexed { index, s ->
                       s to scores[index]
                   }.toTypedArray())
                    Log.i("TAG", "onResults: $marged")
                    val speechResult = recData.first()


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
                    hearText = recData.first()
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
    private val regex = "[0-9]+[???]".toRegex()

    enum class SpeechState(val message: String?) {
        None(""),
        Ready("?????????????????????"),
        Listening("?????????"),

        //????????????
        Knowing(null),
        Done(null),
        Error("??????????????????????????????")
    }

    var speechSoundLevel by mutableStateOf(0F)
    var speechState by mutableStateOf(SpeechState.None)
    var hearText by mutableStateOf<String?>(null)

    fun parseHearText(): Pair<String, Int?> =
        hearText!!.replace(regex, "") to regex.find(hearText!!)?.value?.replace("???", "0000")?.replaceFirst("???", "")
            ?.toIntOrNull()

    fun startSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3)
        }
        speechRecognizer.startListening(intent)
    }

    fun cancelSpeech() {
        speechRecognizer.cancel()
        speechState = SpeechState.None
        speechSoundLevel = 0F
        Toast.makeText(getApplication(), "??????????????????????????????", Toast.LENGTH_LONG).show()
    }


    private fun errorCodeToMessage(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "????????????????????????"
        SpeechRecognizer.ERROR_CLIENT -> "???????????????????????????"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "??????????????????????????????"
        SpeechRecognizer.ERROR_NETWORK -> "???????????????????????????"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "?????????????????????????????????????????????"
        SpeechRecognizer.ERROR_NO_MATCH -> "??????????????????????????????"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "???????????????????????????"
        SpeechRecognizer.ERROR_SERVER -> "?????????????????????"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "??????????????????????????????"
        /*SpeechRecognizer.ERROR_TOO_MANY_REQUESTS,
        SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE->*/
        else -> "??????????????????"
    }

    fun addItemData(date: Date, name: String, price: String, categoryId: Int) {
        if (name.isNotBlank() && price.isNotBlank() && price.isDigitsOnly()) {
            ioThread({_,e->
                if (e == null) {
                    Looper.prepare()
                    Toast.makeText(getApplication(),"??????????????????????????????",Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
            }) {itemDataRepository.insertItemData(ItemData(date = date, name = name, price = price.toInt(), categoryId = categoryId))}
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)

@Composable
fun AddItemBody(
    date: Date,
    toNavigateFunc: NavigateFunc,
    viewModel: AddItemViewModel = hiltViewModel(),
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
            Text("????????????????????????")
        }, text = {
            Column {
                Text("??????100?????????????????????????????????????????????????????????????????????")
                Text("?????????????????????????????????????????????????????????")
                Text("??????????????????????????????????????????????????????????????????????????????????????????")
            }

        }, confirmButton = {
            TextButton({ helpDialogOpen = false }) {
                Text("OK")
            }
        })
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text("??????????????????")
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
                Text("?????????????????????")
            }
            OutlinedTextField(name, onValueChange = { name = it }, label = { Text("??????") }, singleLine = true)
            OutlinedTextField(
                price,
                onValueChange = {
                    if (it.isDigitsOnly()) {
                        price = it.trimStart { it == '0' }
                    }
                },
                label = { Text("??????") },
                trailingIcon = { Text("???", color = MaterialTheme.colors.onSurface) },
                placeholder = { Text("????????????") },
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
                                Text("????????????")
                            } else {
                                Icon(Icons.Default.Cancel, null)
                                Text("???????????????")
                            }

                        }
                        IconButton({
                            helpDialogOpen = true
                        }) {
                            Icon(Icons.Default.Help, null)
                        }
                    }
                    is PermissionStatus.Denied -> {
                        Text("????????????????????????????????????????????????????????????")
                        Button({
                            micPermissionState.launchPermissionRequest()
                        }) {
                            Text("??????")
                        }
                    }
                }
            }
            Text(viewModel.speechState.message ?: viewModel.hearText!!)
            if (viewModel.speechState == AddItemViewModel.SpeechState.Done) {
                val (resultName, resultPrice) = viewModel.parseHearText()
                name = resultName.ifEmpty { name }
                price = (resultPrice)?.toString()?:price
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
            val allCategory by viewModel.allCategories.observeAsState()
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
                        CategoriesDropDown(expanded,{expanded=false},allCategory.orEmpty()){
                            expanded=false
                            selectedCategoryId=it.id
                        }
                    }
                }
            }
            val canSubmit = name.isNotBlank() && price.isNotBlank() && price.isDigitsOnly()
            Button({
                viewModel.addItemData(date,name, price, selectedCategoryId)
            }, enabled = canSubmit) {
                Icon(Icons.Default.Add, null)
                Text("??????????????????")
            }
            if (!canSubmit) {
                Text("???????????????????????????????????????????????????????????????")
            }
        }
    }

}



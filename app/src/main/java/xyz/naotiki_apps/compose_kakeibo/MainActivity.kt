package xyz.naotiki_apps.compose_kakeibo


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.FormatType
import xyz.naotiki_apps.compose_kakeibo.KakeiboScreen.Companion.NAVIGATE_TO_BACK
import xyz.naotiki_apps.compose_kakeibo.KakeiboScreen.Companion.buildGraph
import xyz.naotiki_apps.compose_kakeibo.Settings.ExportPath
import xyz.naotiki_apps.compose_kakeibo.Settings.OmitYen
import xyz.naotiki_apps.compose_kakeibo.Settings.Splitter
import xyz.naotiki_apps.compose_kakeibo.ui.theme.Compose_kakeiboTheme
import javax.inject.Inject

//カテゴリFlowとかここに移したほうが・・・
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    init {
        //TODO なのであとでやる
        //小泉構文
        throw NotImplementedError()
    }

}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val mainViewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences=getSharedPreferences(SHARED_PREFERENCES_SETTINGS, MODE_PRIVATE)
        Settings.init(sharedPreferences)
        DateStringFormatter.init(Splitter.castedValue<FormatType>()!!)
        setContent {
            Compose_kakeiboTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    var nowScreen by remember { mutableStateOf(KakeiboScreen.Summary) }
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        nowScreen = KakeiboScreen.fromRoute(destination.route!!)
                    }

                    Scaffold(bottomBar = {
                        BottomNavigation() {
                            BottomNavigationItem(nowScreen == KakeiboScreen.Summary, {
                                navController.navigate(KakeiboScreen.Summary.name)
                            }, icon = { Icon(Icons.Default.Summarize, null) }, label = { Text("概要") })
                            BottomNavigationItem(nowScreen == KakeiboScreen.Detail, {
                                navController.navigate(KakeiboScreen.Detail.name)
                            }, icon = { Icon(Icons.Default.ViewList, null) }, label = { Text("詳細") })
                        }
                    }) {
                        NavHost(navController, KakeiboScreen.CategorySettings.name, Modifier.padding(it)) {
                            buildGraph { route: String ->
                                if (route == NAVIGATE_TO_BACK)
                                    navController.popBackStack()
                                else
                                    navController.navigate(route) {
                                        restoreState = true
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
}


package xyz.naotiki_apps.compose_kakeibo


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.FormatType
import xyz.naotiki_apps.compose_kakeibo.KakeiboScreen.Companion.NAVIGATE_TO_BACK
import xyz.naotiki_apps.compose_kakeibo.KakeiboScreen.Companion.buildGraph
import xyz.naotiki_apps.compose_kakeibo.ui.theme.Compose_kakeiboTheme
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val mainViewModel by viewModels<MainViewModel>()
    @Composable
    fun CategoriesIcon(text: IconText) {
        Box(modifier = Modifier.size(50.dp).background(Color.LightGray, RoundedCornerShape(50))) {
            Text(
                text.iconText,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }


    @Preview
    @Composable
    fun CategoriesIconPre() {
        CategoriesIcon(IconText("\uD842\uDFB7"))
    }

    @Preview
    @Composable
    fun CategoriesIconPre2() {
        CategoriesIcon(IconText("🍔"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DateStringFormatter.init(FormatType.Char)
        // appDatabase = AppDatabase.getInstance(this)

        setContent {
            Compose_kakeiboTheme {
                //AddItemBody()
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    var nowScreen by remember { mutableStateOf(KakeiboScreen.Summary) }
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        nowScreen = KakeiboScreen.fromRoute(destination.route!!)
                    }

                    Scaffold(topBar = {
                    },bottomBar = {
                        BottomNavigation() {
                            BottomNavigationItem(nowScreen == KakeiboScreen.Summary, {
                                navController.navigate(KakeiboScreen.Summary.name)
                            }, icon = { Icon(Icons.Default.Summarize, null) }, label = { Text("概要") })
                            BottomNavigationItem(nowScreen == KakeiboScreen.Detail, {
                                navController.navigate(KakeiboScreen.Detail.name)
                            }, icon = { Icon(Icons.Default.ViewList, null) }, label = { Text("詳細") })
                        }
                    }) {
                        NavHost(navController, KakeiboScreen.Summary.name, Modifier.padding(it)) {
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


                    // CategoriesList()
                    /*
                     /* val scaffoldState = rememberBottomSheetScaffoldState()
         */
                     /* BottomSheetScaffold(
                          sheetContent = {
                              if (scaffoldState.bottomSheetState) {
                                  TopAppBar (title = { Text("App") })
                              }
                              Column(modifier = Modifier.verticalScroll(sheetScrollState)) {
                                  Text(selectedDate?.toString() ?: "選択してください", style = MaterialTheme.typography.h4)
                                  for (i in 1..10) {
                                      val bool = i % 2 == 0
                                      Box(
                                          modifier = Modifier.background(if (bool) Color.Gray else Color.Black)
                                              .size(50.dp, 150.dp)
                                      )
                                  }


                              }


                          }, sheetPeekHeight = 100.dp
                      ) {

                      }*/*/
                }
            }
        }
    }
}


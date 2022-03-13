package xyz.naotiki_apps.compose_kakeibo


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import xyz.naotiki_apps.compose_kakeibo.DateRange.Companion.asOneDayDateRange
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.FormatType
import xyz.naotiki_apps.compose_kakeibo.KakeiboScreen.Companion.NAVIGATE_TO_BACK
import xyz.naotiki_apps.compose_kakeibo.KakeiboScreen.Companion.buildGraph
import xyz.naotiki_apps.compose_kakeibo.ui.theme.Compose_kakeiboTheme
import javax.inject.Inject
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.associateWith
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.ifEmpty
import kotlin.collections.map
import kotlin.collections.set

//一文字だけ
data class IconText(var iconText: String) {
    init {
        iconText = iconText.firstLetter()
    }
}

//これSummaryVMでいいんじゃね？？
@HiltViewModel
class MainViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    private val itemDataRepository: ItemDataRepository
) : ViewModel() {

    var allCategories = categoryRepository.getAllCategories().distinctUntilChanged()

    var summary: LiveData<List<DateAndPriceProductItem>>? = null


    fun getSummary(date: Date, isMonth: Boolean = false) {
        summary =
            itemDataRepository.getDaySummary(if (isMonth) DateRange(date) else date.asOneDayDateRange())
                .distinctUntilChanged().asLiveData()
    }

    var events: LiveData<List<Date>>? = null
    fun getHasDataDays(dateRange: DateRange) {
        events = itemDataRepository.getHasDataDay(dateRange).asLiveData().distinctUntilChanged()
    }

    fun getAllCategoriesWithChildren(categories: List<Category>): Map<Category, List<Category>?> {
        val parents = categories.filter {
            it.parentId == null
        }
        val children = categories.filter {
            it.parentId != null
        }
        return parents.associateWith {
            children.filter { child ->
                child.parentId == it.id
            }.ifEmpty { null }
        }
    }

}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Composable
    fun CategoriesList() {
        val allCategory by mainViewModel.allCategories.collectAsState(null)
        val scrollState = rememberScrollState()
        if (allCategory != null) {
            val categoriesTree = mainViewModel.getAllCategoriesWithChildren(allCategory!!)
            val expanded = remember {
                categoriesTree.keys.map {
                    it to false
                }.toMutableStateMap()
            }
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                categoriesTree.forEach { (parent, children) ->
                    val hasChildren = children != null
                    val isExpanded = expanded[parent]!!
                    Row(Modifier.fillMaxWidth()) {
                        if (hasChildren) {
                            IconButton(
                                onClick = { expanded[parent] = !isExpanded }
                            ) {
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null
                                )
                            }
                        } else {
                            Spacer(Modifier.width(50.dp))
                        }

                        Text(parent.name, modifier = Modifier.weight(1F))
                    }
                    if (hasChildren && isExpanded) {
                        children!!.forEach {
                            Row(Modifier.fillMaxWidth()) {
                                Text(it.name, modifier = Modifier.weight(1F))
                            }
                        }
                    }
                }
            }
        } else {
            Text("ぬる！！！！")
        }
    }

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

    private val mainViewModel: MainViewModel by viewModels()

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
//KakeiboScreen.AddItem.name+"/{date}"
                    NavHost(navController, KakeiboScreen.Summary.name) {
                        buildGraph { route: String ->
                            if (route == NAVIGATE_TO_BACK)
                                navController.popBackStack()
                            else
                                navController.navigate(route)

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


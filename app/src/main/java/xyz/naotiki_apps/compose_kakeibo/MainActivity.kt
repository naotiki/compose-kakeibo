package xyz.naotiki_apps.compose_kakeibo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import xyz.naotiki_apps.compose_kakeibo.CalendarUtil.DayOfWeek
import xyz.naotiki_apps.compose_kakeibo.DateRange.Companion.asOneDayDateRange
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.FormatType
import xyz.naotiki_apps.compose_kakeibo.ui.theme.Compose_kakeiboTheme

//一文字だけ
data class IconText(var iconText: String) {
    init {
        iconText = iconText.firstLetter()
    }

}

class MainViewModel(
    appDatabase: AppDatabase
) : ViewModel() {

    private val categoryRepository: CategoryRepository = CategoryRepository(appDatabase.categoryDao())
    private val productItemRepository: ProductItemRepository = ProductItemRepository(appDatabase.productItemDao())
    var allCategories = categoryRepository.getAllCategories().distinctUntilChanged()

    var summary = MutableStateFlow<List<DateAndPriceProductItem>?>(null)


    fun getSummary(date: Date, isMonth: Boolean = false) {

        ioThread {
            summary.value =
                productItemRepository.getDaySummary(if (isMonth) DateRange(date) else date.asOneDayDateRange())
        }
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

class MainActivity : ComponentActivity() {
    //lateinit var appDatabase: AppDatabase


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
                                onClick = { expanded[parent] = !isExpanded },
                                enabled = hasChildren
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

    private val mainViewModel by viewModels<MainViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(AppDatabase.getInstance(this@MainActivity)) as T
            }
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



    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DateStringFormatter.init(FormatType.Char)
        // appDatabase = AppDatabase.getInstance(this)


        setContent {
            Log.i("TAGGG", Date.getToday().toString())
            val calendarState = rememberCalendarViewState()
            Compose_kakeiboTheme {
                //AddItemBody()
                Surface(color = MaterialTheme.colors.background) {
                    val navController= rememberNavController()
                    NavHost(navController,KakeiboScreen.AddItem.name){
                        KakeiboScreen.values().forEach {
                            composable(it.name){_->
                                it.body()
                            }
                        }
                    }
                    // CategoriesList()
                   /* val scope = rememberCoroutineScope()
                    val sheetScrollState = rememberScrollState()

                    val scaffoldState = rememberBottomSheetScaffoldState()
                    BottomSheetScaffold(topBar = {
                        TopAppBar(title = {
                            Text("Kakeibo App β")
                        }, actions = {
                            IconButton({}) {
                                Icon(Icons.Default.Settings, null)
                            }
                        })
                    }, floatingActionButton = {

                        if (calendarState.selectedDate != null && scaffoldState.bottomSheetState.isCollapsed) {
                            ExtendedFloatingActionButton(icon = {
                                Icon(Icons.Default.Add, null)
                            }, text = {

                                Text("追加")

                            }, onClick = {
                                TODO("追加処理")
                            })
                        }
                    }, scaffoldState = scaffoldState, sheetPeekHeight = 150.dp,
                        sheetContent = {
                            Column(modifier = Modifier.fillMaxHeight().verticalScroll(sheetScrollState)) {
                                val summary = mainViewModel.summary.collectAsState()
                                if (scaffoldState.bottomSheetState.isCollapsed) {
                                    Text(
                                        calendarState.selectedDate?.toString()
                                            ?: calendarState.currentDate.toStringIgnoreDay(),
                                        style = MaterialTheme.typography.h4
                                    )
                                    summary.value?.also {
                                        Text("${it.size}件のデータ")
                                        var sumPrice = 0
                                        it.forEach { priceProductItem ->
                                            sumPrice += priceProductItem.price
                                        }
                                        Text("合計:${sumPrice}円")
                                    }


                                } else {
                                    TopAppBar(title = {
                                        Text(
                                            calendarState.selectedDate?.toString()
                                                ?: calendarState.currentDate.toStringIgnoreDay()
                                        )
                                    }, navigationIcon = {
                                        IconButton({}) {
                                            Icon(Icons.Default.ArrowBack, null)
                                        }
                                    })
                                }


                            }
                        }) {


                        CalendarView(DayOfWeek.Sunday, onScrolled = {

                        }, onDaySelected = { date ->
                            mainViewModel.getSummary(date ?: calendarState.currentDate, date == null)
                            Toast.makeText(this@MainActivity, "$date", Toast.LENGTH_LONG).show()
                        }, state = calendarState)


                    }
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


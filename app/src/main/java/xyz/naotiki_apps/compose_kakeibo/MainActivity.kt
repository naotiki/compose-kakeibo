package xyz.naotiki_apps.compose_kakeibo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import xyz.naotiki_apps.compose_kakeibo.CalendarUtil.DayOfWeek
import xyz.naotiki_apps.compose_kakeibo.ui.theme.Compose_kakeiboTheme

//一文字だけ
data class IconText(var iconText: String) {
    init {
        iconText = iconText.firstLetter()
    }

}

class MainViewModel(
    private val appDatabase: AppDatabase
) : ViewModel() {
    val categoryRepository: CategoryRepository = CategoryRepository(appDatabase.categoryDao())
    var allCategories = categoryRepository.getAllCategories().distinctUntilChanged()

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

        // appDatabase = AppDatabase.getInstance(this)


        setContent {

            var selectedDate: Date? by remember { mutableStateOf(Date.getToday()) }
            Compose_kakeiboTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    // CategoriesList()
                    val scope = rememberCoroutineScope()

                    val scaffoldState = rememberBottomSheetScaffoldState()

                    val sheetScrollState = rememberScrollState()
                    BottomSheetScaffold(scaffoldState = scaffoldState,
                        topBar = {
                            TopAppBar(title = {
                                Text("家計簿！！")
                            }, actions = {

                            })
                        },
                        floatingActionButton = {
                            FloatingActionButton({}){
                                Icon(Icons.Default.Add, null)
                            }
                        },
                        sheetContent = {
                            Column(modifier = Modifier.verticalScroll(sheetScrollState)) {
                                Text(selectedDate?.toString() ?: "選択してください")
                                for(i in 1 .. 10){
                                    val bool=i%2==0
                                    Box(modifier = Modifier.background(if(bool)Color.Gray else Color.Black).size(50.dp,150.dp))
                                }


                            }


                        }, sheetPeekHeight = 300.dp) {
                        CalendarView2(Date.getToday(true), selectedDate, DayOfWeek.Sunday, onScrolled = {

                        }, onDaySelected = { date ->
                            selectedDate = date
                            Toast.makeText(this, "$date", Toast.LENGTH_LONG).show()
                        })
                    }
                }
            }
        }
    }
}


@Composable
fun CalendarView2(

    initData: Date,
    selectedDate: Date?,
    firstDayOfDayOfWeek: DayOfWeek,
    onDaySelected: (Date) -> Unit,
    onScrolled: (Direction) -> Unit = {}
) {
    var currentDate by remember { mutableStateOf(initData) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val loopPagerState = rememberLoopPagerState()
    Box(Modifier.padding(top = 10.dp).background(Color(0x3F, 0x3F, 0x3F), RoundedCornerShape(10.dp))) {
        Column {
            Row(
                Modifier.width(screenWidth)
                    .background(MaterialTheme.colors.primary, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            ) {
                IconButton({
                    loopPagerState.animateTo(Direction.Previous)
                }, Modifier.weight(0.5F)) {
                    Icon(Icons.Filled.NavigateBefore, null)
                }
                Text(
                    currentDate.toStringIgnoreDay(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1F),
                    style = MaterialTheme.typography.h4
                )
                IconButton({
                    loopPagerState.animateTo(Direction.Next)
                }, Modifier.weight(0.5F)) {
                    Icon(Icons.Filled.NavigateNext, null)
                }
            }
            LoopPager(loopPagerState, {
                onScrolled(it)
                currentDate = currentDate.shiftMonth(it)
            }, {
                MonthView(
                    selectedDate, currentDate.shiftMonth(Direction.Previous), false, firstDayOfDayOfWeek
                )
            }, {
                MonthView(selectedDate, currentDate, true, firstDayOfDayOfWeek) {
                    //selectedDate = it
                    onDaySelected(it)
                }
            }, {
                MonthView(
                    selectedDate, currentDate.shiftMonth(Direction.Next), false, firstDayOfDayOfWeek
                )
            })


        }

    }
}

@Composable
fun MonthView(
    selectedDate: Date?,
    date: Date,
    isCurrentMonth: Boolean,
    firstDayOfDayOfWeek: DayOfWeek,
    onDaySelected: ((date: Date) -> Unit)? = null
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Column(
        Modifier/*.height(355.dp)*/.padding(vertical = 3.dp).width(screenWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(/*Modifier.align(Alignment.CenterHorizontally)*/) {
            var dayOfWeek = firstDayOfDayOfWeek
            for (i in 0..6) {
                Text(
                    dayOfWeek.str, color = when (dayOfWeek) {
                        DayOfWeek.Sunday -> Color.Red
                        DayOfWeek.Saturday -> Color.Blue
                        else -> Color.Unspecified
                    }, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center
                )



                dayOfWeek = dayOfWeek.slideToNextWithLoop()
            }
        }
        val days = CalendarUtil.dateScope(date) {
            splitDaysToWeek(getMonthDayOfWeeks(), firstDayOfDayOfWeek)
        }
        var dayCount = 1
        days.forEach {
            Row(/*Modifier.align(Alignment.CenterHorizontally)*/) {
                it.forEach {
                    if (it == null) {
                        Spacer(Modifier.size(50.dp))
                    } else {
                        Day(dayCount,
                            isSelected = date.copy(day = dayCount) == selectedDate,
                            hasData = false,
                            textColor = when (it) {
                                DayOfWeek.Sunday -> Color.Red
                                DayOfWeek.Saturday -> Color.Blue
                                else -> Color.Unspecified
                            },
                            onClick = { day ->

                                if (onDaySelected != null) {
                                    onDaySelected(date.copy(day = day))
                                }
                            })
                        dayCount++
                    }

                }
            }
        }
        if (days.size < 6) {
            Row { for (i in 0..6) Spacer(Modifier.size(50.dp)) }
        }
    }
}


@Composable
fun Day(
    day: Int,
    isSelected: Boolean = false,
    hasData: Boolean = false,
    textColor: Color = Color.Unspecified,
    onClick: (Int) -> Unit = {}
) {
    Box(Modifier.size(50.dp).clickable(onClick = { onClick(day) })) {
        Box(
            Modifier.fillMaxSize().padding(1.dp).background(
                MaterialTheme.colors.background, RoundedCornerShape(10)
            )
        )
        if (isSelected) {
            Box(Modifier.fillMaxSize().padding(4.dp).background(Color(30, 186, 163, 0x5F), RoundedCornerShape(25)))
        }
        Text((day ?: "").toString(), Modifier.align(Alignment.Center), textColor)
        if (hasData) {
            Icon(
                Icons.Filled.Circle,
                null,
                Modifier.size(15.dp).align(Alignment.BottomCenter).padding(bottom = 7.dp),
                tint = Color.Blue
            )
        }
    }
}

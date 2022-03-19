package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.naotiki_apps.compose_kakeibo.CalendarUtil.DayOfWeek
import xyz.naotiki_apps.compose_kakeibo.DateRange.Companion.asOneDayDateRange
import javax.inject.Inject

//これSummaryVMでいいんじゃね？？->YES!!!!
@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val itemDataRepository: ItemDataRepository
) : ViewModel() {
    //StateFlow自体を状態にすることでFlowが変更されても対処
    var summaryFlow by mutableStateOf<StateFlow<List<ItemData>>?>(null)
    fun getSummary(dateRange: DateRange) {
        summaryFlow = itemDataRepository.getDaySummary(dateRange)
            .stateIn(viewModelScope, WhileSubscribed(1000), emptyList())
    }


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SummaryBody(summaryViewModel: SummaryViewModel = hiltViewModel(), toAddItemScreen: (date: Int) -> Unit) {

    val calendarState = rememberCalendarViewState()

    val summary = summaryViewModel.summaryFlow?.collectAsState()
    LaunchedEffect(true) {
        summaryViewModel.getSummary(DateRange(calendarState.currentDate))
    }

    val scope = rememberCoroutineScope()
    val sheetScrollState = rememberScrollState()

    var summaryFilterByRange by remember { mutableStateOf(DateRange(calendarState.currentDate)) }
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(topBar = { TopAppBar(title = { Text("家計簿 β") }) }, floatingActionButton = {


        Column {
            if (calendarState.selectedDate != null && bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                ExtendedFloatingActionButton(icon = {
                    Icon(Icons.Default.Add, null)
                }, text = {

                    Text("追加")

                }, onClick = { toAddItemScreen(calendarState.selectedDate!!.toInt()) })
            }

        }
    }, scaffoldState = bottomSheetScaffoldState, sheetPeekHeight = 150.dp, sheetContent = {
        Column(modifier = Modifier.fillMaxHeight().verticalScroll(sheetScrollState)) {

            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                Text(
                    calendarState.selectedDate?.toString() ?: calendarState.currentDate.toStringIgnoreDay(),
                    style = MaterialTheme.typography.h4
                )
                summary?.value?.filter {
                    it.date in summaryFilterByRange
                }.also {
                    Text("${it?.size ?: 0}件のデータ")
                    var sumPrice = 0
                    it?.forEach { priceProductItem ->
                        sumPrice += priceProductItem.price
                    }
                    Text("合計:${sumPrice}円")
                }


            } else {
                TopAppBar(title = {
                    Text(
                        calendarState.selectedDate?.toString() ?: calendarState.currentDate.toStringIgnoreDay()
                    )
                }, navigationIcon = {
                    IconButton({
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                })
            }


        }
    }) {


        CalendarView(DayOfWeek.Sunday, eventDays = summary?.value.orEmpty().map { it.date }.toSet(), onScrolled = {
            scope.launch {
                summaryViewModel.getSummary(DateRange(it))
            }
        }, onDaySelected = {
            summaryFilterByRange = it?.asOneDayDateRange() ?: DateRange(calendarState.currentDate)
        }, state = calendarState)


    }
}

//TODO 概要円グラフの実装
@Composable
fun SummaryPieChart(itemDataList: List<ItemData>, categoryList: List<Category>) {

    AndroidView({
        PieChart(it).apply {
            categoryList.map { it.id }
            setOnChartValueSelectedListener(object:OnChartValueSelectedListener{
                override fun onValueSelected(e: Entry?, h: Highlight?) {

                }

                override fun onNothingSelected() {

                }

            })

            itemDataList.forEach { }
        }
    })
}

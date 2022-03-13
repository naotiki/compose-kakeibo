package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SummaryBody(mainViewModel: MainViewModel = hiltViewModel(), toAddItemScreen: (date: Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val calendarState = rememberCalendarViewState()
    val sheetScrollState = rememberScrollState()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()


    BottomSheetScaffold(topBar = { TopAppBar(title = { Text("家計簿 β") }) },
        floatingActionButton = {

            if (calendarState.selectedDate != null && bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                ExtendedFloatingActionButton(icon = {
                    Icon(Icons.Default.Add, null)
                }, text = {

                    Text("追加")

                }, onClick = { toAddItemScreen(calendarState.selectedDate!!.toInt()) })
            }
        },
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 150.dp,
        sheetContent = {
            Column(modifier = Modifier.fillMaxHeight().verticalScroll(sheetScrollState)) {
                val summary = mainViewModel.summary?.observeAsState()
                if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                    Text(
                        calendarState.selectedDate?.toString()
                            ?: calendarState.currentDate.toStringIgnoreDay(),
                        style = MaterialTheme.typography.h4
                    )
                    summary?.value.also {
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
                            calendarState.selectedDate?.toString()
                                ?: calendarState.currentDate.toStringIgnoreDay()
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

       /* LaunchedEffect(calendarState.currentDate) {
            launch {
                mainViewModel.getHasDataDays(range)
            }
        }*/
        CalendarView(CalendarUtil.DayOfWeek.Sunday, eventBuilder = {

        }, onDaySelected = { date ->
            mainViewModel.getSummary(date ?: calendarState.currentDate, date == null)
        }, state = calendarState)


    }
}

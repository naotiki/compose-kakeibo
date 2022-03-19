package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.Companion.MONTH
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.Companion.YEAR
import xyz.naotiki_apps.compose_kakeibo.DateStringFormatter.Companion.isDataFormat

class CalendarViewState(currentDate: Date, selectedDate: Date?) {
    var currentDate: Date by mutableStateOf(currentDate)
    var selectedDate: Date? by mutableStateOf(selectedDate)
}

val calendarViewStateSaver = run {
    val currentDateKey = "CurrentDate"
    val selectedDateKey = "SelectedDate"
    mapSaver(
        save = { mapOf(currentDateKey to it.currentDate, selectedDateKey to it.selectedDate) },
        restore = { CalendarViewState(it[currentDateKey] as Date, it[selectedDateKey] as Date?) }
    )
}

@Composable
fun rememberCalendarViewState(
    currentDate: Date = Date.getToday(true),
    selectedDate: Date? = Date.getToday()
): CalendarViewState {
    return rememberSaveable(saver = calendarViewStateSaver) {
        CalendarViewState(currentDate, selectedDate)
    }
}


@Composable
fun CalendarView(
    firstDayOfDayOfWeek: CalendarUtil.DayOfWeek,
    eventDays: Set<Date>,
    onDaySelected: (Date?) -> Unit,
    onScrolled: (currentData:Date) -> Unit = {},

    state: CalendarViewState = rememberCalendarViewState(),
) {
    val currentOnDaySelected by rememberUpdatedState(onDaySelected)
    val currentOnScrolled by rememberUpdatedState(onScrolled)

    var dialogOpen by remember { mutableStateOf(false) }
    if (dialogOpen) {
        Dialog(onDismissRequest = {
            dialogOpen = false
        }) {
            val yearTextFocus = remember { FocusRequester() }
            val monthTextFocus = remember { FocusRequester() }
            var yearText by remember(state.currentDate) {
                mutableStateOf(
                    TextFieldValue(
                        state.currentDate.year.toString(),
                        TextRange(state.currentDate.year.toString().length)
                    )
                )
            }
            var monthText by remember(state.currentDate) {
                mutableStateOf(
                    TextFieldValue(
                        state.currentDate.month.toString(),
                        TextRange(state.currentDate.month.toString().length)
                    )
                )
            }
            Surface(color = MaterialTheme.colors.background, modifier = Modifier.clip(RoundedCornerShape(4.dp))) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("日付を選択")
                    Row {
                        OutlinedTextField(
                            yearText, modifier = Modifier.focusRequester(yearTextFocus),
                            label = { Text("年") },
                            onValueChange = {
                                if (it.text.isDataFormat(YEAR)) yearText = it.copy(text = it.text.trimStart('0'))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ), keyboardActions = KeyboardActions {
                                monthTextFocus.requestFocus()
                            }, isError = yearText.text.isEmpty()
                        )
                    }
                    LaunchedEffect(Unit) {
                        yearTextFocus.requestFocus()
                    }
                    Row {
                        OutlinedTextField(
                            monthText,
                            label = { Text("月") },
                            modifier = Modifier.focusRequester(monthTextFocus),
                            onValueChange = {
                                if (it.text.isDataFormat(MONTH)) monthText = it.copy(text = it.text.trimStart('0'))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            keyboardActions = KeyboardActions {
                                if (yearText.text.isNotEmpty() && monthText.text.isNotEmpty()) {
                                    state.currentDate = Date(yearText.text.toInt(), monthText.text.toInt())
                                    dialogOpen = false
                                }
                            }, isError = monthText.text.isEmpty()
                        )
                    }
                    Button({
                        state.currentDate = Date(yearText.text.toInt(), monthText.text.toInt())
                        dialogOpen = false
                    }, enabled = yearText.text.isNotEmpty() && monthText.text.isNotEmpty()) {
                        Text("移動")
                    }
                }
            }
        }
    }


    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val loopPagerState = rememberLoopPagerState()
    Box(
        Modifier.padding(top = 10.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                state.selectedDate = null
                currentOnDaySelected(null)
            }.background(MaterialTheme.colors.secondary, RoundedCornerShape(10.dp))
    ) {
        Column {
            Row(
                Modifier.width(screenWidth)
                    .background(MaterialTheme.colors.primary, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            ) {
                IconButton({
                    loopPagerState.animateTo(Direction.Previous)
                }, Modifier.weight(0.5F)) {
                    Icon(Icons.Filled.NavigateBefore, null, tint = MaterialTheme.colors.onPrimary)
                }

                Text(
                    state.currentDate.toStringIgnoreDay(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1F)
                        .clickable {
                            dialogOpen = true
                        },
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h4
                )
                IconButton({
                    loopPagerState.animateTo(Direction.Next)
                }, Modifier.weight(0.5F)) {
                    Icon(Icons.Filled.NavigateNext, null, tint = MaterialTheme.colors.onPrimary)
                }
            }

            LoopPager(loopPagerState, { direction ->
                state.currentDate = state.currentDate.shiftMonth(direction)
                currentOnScrolled(state.currentDate)
            }, {

                MonthView(
                    state.selectedDate,
                    state.currentDate.shiftMonth(Direction.Previous),
                    false,
                    firstDayOfDayOfWeek,
                )
            }, {

                MonthView(
                    state.selectedDate,
                    state.currentDate,
                    true,
                    firstDayOfDayOfWeek,
                    eventDays.filter { it in DateRange(state.currentDate) }.toSet()
                ) {
                    state.selectedDate = it
                    currentOnDaySelected(it)
                }
            }, {

                MonthView(
                    state.selectedDate, state.currentDate.shiftMonth(Direction.Next), false, firstDayOfDayOfWeek,
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
    firstDayOfDayOfWeek: CalendarUtil.DayOfWeek,
    eventDays: Set<Date>? = null,
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
                        CalendarUtil.DayOfWeek.Sunday -> Color.Red
                        CalendarUtil.DayOfWeek.Saturday -> Color.Blue
                        else -> MaterialTheme.colors.onSecondary
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
                        val dayDate = date.copy(day = dayCount)

                        Day(dayCount, isToDay = dayDate == Date.getToday(),
                            isSelected = dayDate == selectedDate,
                            hasEvent = eventDays?.contains(dayDate) ?: false,
                            textColor = when (it) {
                                CalendarUtil.DayOfWeek.Sunday -> Color.Red
                                CalendarUtil.DayOfWeek.Saturday -> Color.Blue
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
    hasEvent: Boolean = false,
    textColor: Color = Color.Unspecified,
    onClick: (Int) -> Unit = {},
    isToDay: Boolean
) {
    Box(Modifier.size(50.dp).clickable(onClick = { onClick(day) })) {
        Box(
            Modifier.fillMaxSize().padding(1.dp).background(
                MaterialTheme.colors.background, RoundedCornerShape(10)
            )
        )
        if (isSelected) {
            Box(Modifier.fillMaxSize().padding(4.dp).background(Color(30, 186, 163, 0x5F), RoundedCornerShape(25)))
        } else if (isToDay) {
            Box(Modifier.fillMaxSize().padding(4.dp).border(2.dp, Color(30, 186, 163, 0x5F), RoundedCornerShape(25)))
        }

        Text(day.toString(), Modifier.align(Alignment.Center), textColor)
        if (hasEvent) {
            Icon(
                Icons.Filled.Circle,
                null,
                Modifier.size(15.dp).align(Alignment.BottomCenter).padding(bottom = 7.dp),
                tint = Color.Blue
            )
        }
    }
}

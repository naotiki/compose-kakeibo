package xyz.naotiki_apps.compose_kakeibo

import xyz.naotiki_apps.compose_kakeibo.Direction.Next
import xyz.naotiki_apps.compose_kakeibo.Direction.Previous
import xyz.naotiki_apps.compose_kakeibo.EnumExtensions.Companion.toEnum
import java.util.*


class DateRange {
    var minDate: Date
    var maxDate: Date

    constructor(date: Date) {
        date.day = 1
        minDate = date
        date.day = CalendarUtil.dateScope(date) {
            getDayOfMonthCount()
        }
        maxDate = date
    }

    constructor(minDate: Date, maxDate: Date) {
        this.minDate = minDate
        this.maxDate = maxDate
    }
}

/**
 * @param month 1<=12
 * */
data class Date(var year: Int, var month: Int, var day: Int = 1) {
    val innerMonth get() = month - 1
    override fun toString(): String {
        return "$year/${month}/$day"
    }

    fun toStringIgnoreDay(): String {

        return "${year}年${month}月"
    }

    //意外と数学って使うのねー
    fun toInt(): Int = (year * 12 + innerMonth) * 32 + day

    operator fun rangeTo(b: Date): DateRange = DateRange(this, b)

    fun shiftMonth(direction: Direction): Date = when (direction) {
            Previous -> if (month == 1) {
                copy(year = year - 1, month = 12)
            } else {
                copy(year = year, month = month - 1)
            }

            Next -> if (month == 12) {
                copy(year = year + 1, month = 1)
            } else {
                copy(year = year, month = month + 1)
            }


        }

    companion object {
        //0から始まるから大丈夫なんですねー
        fun fromInt(int: Int): Date {
            val day = (int % 32)
            val y12m = (int - day) / 32
            val month = (y12m % 12)
            val year = ((y12m - month) / 12)
            return Date(year, month + 1, day)
        }

        private val calendar: Calendar = Calendar.getInstance(Locale("ja_JP@calendar=japanese"))
        fun getToday(ignoreDay: Boolean = false): Date {

            calendar.timeInMillis = System.currentTimeMillis()

            return Date(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                if (ignoreDay) 1 else calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

class CalendarUtil private constructor() {
    enum class DayOfWeek(val str: String) {
        Sunday("日"),
        Monday("月"),
        Tuesday("火"),
        Wednesday("水"),
        Thursday("木"),
        Friday("金"),
        Saturday("土");


    }


    private val calendar: Calendar = Calendar.getInstance(Locale("ja_JP@calendar=japanese"))


    fun getMonthDayOfWeeks(): List<DayOfWeek> {
        val dayCount = getDayOfMonthCount()
        val firstDayOfWeek = getMonthFirstDayOfWeek()
        val days = mutableListOf<DayOfWeek>()
        var currentDayOfDayOfWeek: DayOfWeek = firstDayOfWeek
        for (i in 1..dayCount) {
            days.add(currentDayOfDayOfWeek)
            currentDayOfDayOfWeek = currentDayOfDayOfWeek.slideToNextWithLoop()
        }
        return days.toList()
    }

    //カレンダーに分割
    fun splitDaysToWeek(days: List<DayOfWeek>, firstDayOfDayOfWeek: DayOfWeek): List<List<DayOfWeek?>> {
        val lastDayOfWeek =
            firstDayOfDayOfWeek.slideToPreviousWithLoop()
        val result = mutableListOf<MutableList<DayOfWeek?>>()
        var weekCount = 0
        var c = firstDayOfDayOfWeek
        val l = mutableListOf<DayOfWeek?>()
        while (c != days.first()) {
            l.add(null)
            c = c.slideToNextWithLoop()
        }
        result.add(l)
        for (i in days.indices) {
            val week = days[i]

            result[weekCount].add(week)

            if (i + 1 == days.size) {
                while (result[weekCount].size < 7) {
                    result[weekCount].add(null)
                }
                break
            }
            if (week == lastDayOfWeek) {
                weekCount++
                result.add(mutableListOf())
            }
        }
        return result
    }

    fun getDayOfMonthCount(): Int {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getMonthFirstDayOfWeek(): DayOfWeek {

        calendar.set(Calendar.DATE, 1)

        return (calendar.get(Calendar.DAY_OF_WEEK) - 1).toEnum<DayOfWeek>()!!
    }

    /*fun <T> dateScope(date: Date, block: CalendarUtil.() -> T): T {
        calendar.set(date.year, date.month, 1)
        val r = this.block()
        calendar.set(currentDate.year, currentDate.month, 1)
        return r
    }*/

    fun setDate(date: Date): CalendarUtil {
        calendar.set(date.year, date.innerMonth, 1)
        return this
    }


    companion object {
        @Volatile
        private var instance = CalendarUtil()

        fun <T> dateScope(date: Date, block: CalendarUtil.() -> T): T {
            return instance.setDate(date).block()
        }
    }
}



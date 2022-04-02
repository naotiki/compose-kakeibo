package xyz.naotiki_apps.compose_kakeibo

import androidx.core.text.isDigitsOnly

class DateStringFormatter(private var formatType: Array<String>) {
    /**
     * @see init
     * */
    enum class FormatType(val spliter: Array<String>) {
        Slash(arrayOf("/", "/", "")),
        Char(arrayOf("年", "月", "日"))
    }

    fun dateToString(date: Date, ignoreDay: Boolean = false): String = buildString {
        append(date.year)
        append(formatType[0])
        append(date.month)
        append(formatType[1])
        if (!ignoreDay) {
            append(date.day)
            append(formatType[2])
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DateStringFormatter? = null

        /**
         * @param formatType [FormatType]から選ぼうね！！
         * */
        fun init(formatType: FormatType): DateStringFormatter {
            return INSTANCE ?: kotlin.run {
                INSTANCE = DateStringFormatter(formatType.spliter)
                return INSTANCE as DateStringFormatter
            }
        }

        fun getInstance(): DateStringFormatter {
            return INSTANCE!!
        }

        const val YEAR = 4
        const val MONTH = 2

        /**
         * ただしいふぉーまっと
         * @param maxLength [YEAR]または[MONTH]をつかおーね！！！
         * */
        fun String.isDataFormat(maxLength: Int) =
            this.length <= maxLength && this.isDigitsOnly()
                    && ((maxLength == MONTH && this.toIntOrNull()
                ?.let { it in 1..12 } == true || this.isEmpty()) || maxLength == YEAR)
    }
}

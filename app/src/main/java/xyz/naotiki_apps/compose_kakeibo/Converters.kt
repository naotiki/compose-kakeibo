package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.ui.graphics.Color
import androidx.room.TypeConverter
import xyz.naotiki_apps.compose_kakeibo.ColorData.Companion.toColorData

class Converters {
    @TypeConverter
    fun toDate(value: Int): Date {
        Color.Red.toColorData()
        return Date.dateFromInt(value)
    }
    @TypeConverter
    fun dateToInt(date: Date): Int {
        return date.toInt()
    }

    @TypeConverter
    fun toColorData(value: Int?): ColorData?{
        return value?.let { ColorData(it) }
    }

    @TypeConverter
    fun colorDataToInt(color: ColorData?):Int?{
        return color?.colorInt
    }
}

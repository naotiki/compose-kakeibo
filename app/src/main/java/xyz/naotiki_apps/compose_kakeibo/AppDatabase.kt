package xyz.naotiki_apps.compose_kakeibo

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById
import xyz.naotiki_apps.compose_kakeibo.ColorData.Companion.toColorData

@Database(
    entities = [ItemData::class, Category::class],
    version = 1,
    exportSchema = true,
    //autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDataDao(): ItemDataDao
    abstract fun categoryDao(): CategoryDao



    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }


        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
                .addCallback(object : Callback() {

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ioThread {
                            getInstance(context).categoryDao().insertAll(*DEFAULT_CATEGORIES)
                        }
                    }
                }).build()


        val DEFAULT_CATEGORIES = arrayOf(
            Category(id = 1, name = "未設定"),
            *Category(id = 2, name = "食品", color = Color.Red.toColorData()).addChildren(
                Category(name = "野菜"),
                Category(name = "肉、魚"),
                Category(name = "主食"),
                Category(name = "嗜好品、果物"),
                Category(name = "惣菜、カップ麺等"),
                Category(name = "調味料、その他")
            ),
            Category(id = 3, name = "日用品", color = Color.Blue.toColorData()),
            Category(id = 4, name = "その他"),
        ).sortById()
    }
}

object Converters {
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

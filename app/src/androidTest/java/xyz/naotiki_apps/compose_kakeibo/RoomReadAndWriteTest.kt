package xyz.naotiki_apps.compose_kakeibo

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById
import xyz.naotiki_apps.compose_kakeibo.ColorData.Companion.toColorData
import xyz.naotiki_apps.compose_kakeibo.DateRange.Companion.asOneDayDateRange
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomReadAndWriteTest {
    private lateinit var db: AppDatabase

    private lateinit var itemDBRepo:ItemDataRepository

    val DEFAULT_CATEGORIES = arrayOf(
        Category(id=1, name = "未設定"),
        *Category(id = 2, name = "食品", color = Color.Red.toColorData()).addChildren(
            Category(name = "野菜", color = null),
            Category(name = "肉、魚", color = null),
            Category(name = "主食", color = null),
            Category(name = "嗜好品、果物", color = null),
            Category(name = "惣菜、カップ麺等", color = null),
            Category(name = "調味料、その他", color = null)
        ),
        Category(id = 3, name = "日用品", color = Color.Blue.toColorData()),
        Category(id = 4, name = "その他", color = Color.Gray.toColorData()),
    ).sortById()
    @Before
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()
        itemDBRepo= ItemDataRepository(db.itemDataDao())
        DateStringFormatter.init(DateStringFormatter.FormatType.Slash)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun readFromDB() = runBlocking{

        Log.v("Test","Hi!")
        Log.v("Test",Date.getToday().asOneDayDateRange().toString())

            db.categoryDao().insertAll( *DEFAULT_CATEGORIES)
            db.itemDataDao().insertAll(
                ItemData(name = "Popcorn", date = Date.getToday(), price = 100, categoryId = 1),
                ItemData(name = "UmaiBooooooo!", date = Date.getToday(), price = 10, categoryId = 2)
            )
        val a= itemDBRepo.getDaySummary(Date.getToday().asOneDayDateRange()).take(1).collect {
            it.forEach {
                Log.v("Test",it.toString())
            }
        }
    }

}

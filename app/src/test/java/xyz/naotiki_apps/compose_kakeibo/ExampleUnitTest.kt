@file:Suppress("RemoveRedundantBackticks", "NonAsciiCharacters")

package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.ui.graphics.Color
import org.junit.Assert
import org.junit.Test
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById
import xyz.naotiki_apps.compose_kakeibo.ColorData.Companion.toColorData

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {

   //String.firstLetter Test
    @Test
    fun firstLetterTest(){
        val test = "test"
        val result = test.firstLetter()
        Assert.assertEquals("t",result)
    }


    @Test
    fun sort(){
        val DEFAULT_CATEGORIES = arrayOf(
            *Category(id = 1, name = "食品", color = Color.Red.toColorData()).addChildren(
                Category(name = "野菜", color = null),
                Category(name = "肉、魚", color = null),
                Category(name = "主食", color = null),
                Category(name = "嗜好品、果物", color = null),
                Category(name = "惣菜、カップ麺等", color = null),
                Category(name = "調味料、その他", color = null)
            ),
            Category(id = 2, name = "日用品", color = Color.Blue.toColorData()),
            Category(id = 3, name = "その他", color = Color.Gray.toColorData()),
        ).sortById()
        println(DEFAULT_CATEGORIES)
    }
    @Test
    fun `日付変換`() {
        var date = Date(2022, 1, 1)
        for (i in 0..36) {
            
           val dayCount= CalendarUtil.dateScope(date.copy()){
                getDayOfMonthCount()
            }
            for (v in 1..dayCount){
                date.day=v
                println(date.toString())
                Assert.assertEquals(date,Date.dateFromInt(date.toInt()))

            }
            date=date.shiftMonth(Direction.Next)
        }

    }
    @Test
    fun addition_isCorrect() {
        IconText("よしだ").toString().println()

    }
    private fun String.println() {
        println(this)
    }
}

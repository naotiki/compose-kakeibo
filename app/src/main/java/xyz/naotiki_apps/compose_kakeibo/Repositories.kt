package xyz.naotiki_apps.compose_kakeibo

import android.util.Log
import kotlinx.coroutines.flow.Flow
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById

class CategoryRepository( private val categoryDao: CategoryDao) {
    fun getAllCategories() = categoryDao.getAllCategory()
    fun addCategories(vararg category: Category) {
        ioThread {
            categoryDao.insertAll(*category.sortById())
        }
    }
}

class ItemDataRepository(private val itemDataDao: ItemDataDao) {
   fun getDaySummary(dateRange: DateRange): Flow<List<DateAndPriceProductItem>> {
       Log.i("TAGGI", "getDaySummary: $dateRange")
       return itemDataDao.getDateRangeSummary(dateRange.minDate, dateRange.maxDate)
   }
    fun getHasDataDay(dataRange: DateRange): Flow<List<Date>> =
        itemDataDao.getHasDataDaysFromDateRange(dataRange.minDate,dataRange.maxDate)

    fun insertItemData(itemData: ItemData){
        ioThread {
            itemDataDao.insertAll(itemData)
        }

    }

}

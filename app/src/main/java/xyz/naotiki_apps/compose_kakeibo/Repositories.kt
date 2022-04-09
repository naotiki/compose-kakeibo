package xyz.naotiki_apps.compose_kakeibo

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById
import xyz.naotiki_apps.compose_kakeibo.ItemDataSort.Companion.sortItemData

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAllCategories() = categoryDao.getAllCategory()
    fun addCategories(vararg category: Category) = ioThread {
        categoryDao.insertAll(*category.sortById())
    }
    fun updateCategory(category: Category) = ioThread {
        categoryDao.update(category)
    }
    fun deleteCategory(category: Category)= ioThread{
        categoryDao.delete(category)
    }

}

class ItemDataRepository(private val itemDataDao: ItemDataDao) {
    fun getDaySummary(dateRange: DateRange): Flow<List<ItemData>> =
        itemDataDao.getDateRangeSummary(dateRange.minDate, dateRange.maxDate)

    /* fun getHasDataDay(dataRange: DateRange): Flow<List<Date>> =
         itemDataDao.getHasDataDaysFromDateRange(dataRange.minDate, dataRange.maxDate)*/

    fun existsItemDataByCategory(category: Category): Boolean = ioThread {
        itemDataDao.existHasCategory(category.id) != null
    }

    fun insertItemData(vararg itemData: ItemData) {
        itemDataDao.insertAll(*itemData)
    }

    fun removeItemData(itemDataList: List<ItemData>) {
        itemDataDao.delete(*itemDataList.toTypedArray())
    }

    //Build SQL sentence
    fun getItemData(
        dataRange: DateRange,
        filterCategoryIds: List<Int>,
        sort: ItemDataSort,
        isDescending: Boolean,
        searchText: String = ""
    ): Flow<List<ItemData>> {
        val d = "${dataRange.minDate.toInt()} <= date AND ${dataRange.maxDate.toInt()} >= date"
        val c =
            if (filterCategoryIds.isNotEmpty()) "and parent_category_id IN (${filterCategoryIds.toStringForSql()})" else ""
        val s = if (searchText.isNotBlank()) "and name like '%$searchText%'" else ""
        val q = SimpleSQLiteQuery("select * from item_data where $d $c $s")
        return itemDataDao.getItemDataViaQuery(q).map { it.sortItemData(sort, isDescending) }
    }

    fun updateItemData(itemDataList: List<ItemData>) {
        itemDataDao.update(*itemDataList.toTypedArray())
    }


}

fun Iterable<Int>.toStringForSql(): String = buildString {
    this@toStringForSql.forEach {
        append(it)
        append(",")
    }
}.removeSuffix(",")

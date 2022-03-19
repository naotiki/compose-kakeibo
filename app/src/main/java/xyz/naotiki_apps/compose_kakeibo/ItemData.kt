package xyz.naotiki_apps.compose_kakeibo

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "item_data", foreignKeys = [ForeignKey(
        onDelete = ForeignKey.SET_DEFAULT,
        entity = Category::class,
        parentColumns = arrayOf("category_id"),
        childColumns = arrayOf("parent_category_id")
    )], indices = [Index("parent_category_id")]
)
data class ItemData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: Date,
    val price: Int,
    @ColumnInfo(name = "parent_category_id", defaultValue = "1") var categoryId: Int,
    val memo: String? = null,
)


enum class ItemDataSort(
    val sortName: String,
    val sortExample: Pair<String, String>,
    val comparator: Comparator<ItemData>
) {
    Name("名前順", "a" to "ん", Comparator { o1, o2 ->
        o1.name.compareTo(o2.name)
    }),
    Price("値段順", "低" to "高", Comparator { o1, o2 ->
        o1.price.compareTo(o2.price)
    }),
    Date("日付順", "古" to "新", Comparator { o1, o2 ->
        if (o1.date.equals(o2.date)) o1.id.compareTo(o2.id) else o1.date.compareTo(o2.date)
    });

    companion object {
        fun Iterable<ItemData>.sortItemData(sortType: ItemDataSort, isDescending: Boolean): List<ItemData> {
            val sorter = if (isDescending) sortType.comparator.reversed() else sortType.comparator
            return this.sortedWith(sorter)
        }
    }
}


data class DateAndPriceProductItem(
    val date: Date,
    val price: Int,
)

@Dao
interface ItemDataDao {
    @Query("SELECT * from item_data WHERE :minDate <= date AND :maxDate >= date")
    fun getDateRangeSummary(minDate: Date, maxDate: Date): Flow<List<ItemData>>

    @Query("SELECT * FROM item_data WHERE date = :date")
    fun getItemsFromDate(date: Date): List<ItemData>

    @Insert
    fun insertAll(vararg users: ItemData)

    @Delete
    fun delete(vararg user: ItemData)

    @Update
    fun update(vararg item: ItemData)

    @RawQuery(observedEntities = [ItemData::class])
     fun getItemDataViaQuery(query: SupportSQLiteQuery): Flow<List<ItemData>>
}



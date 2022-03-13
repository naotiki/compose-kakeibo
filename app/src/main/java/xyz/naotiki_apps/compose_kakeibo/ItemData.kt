package xyz.naotiki_apps.compose_kakeibo

import android.content.Context
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById
import xyz.naotiki_apps.compose_kakeibo.ColorData.Companion.toColorData

@Entity(
    tableName = "item_data", foreignKeys = [ForeignKey(onDelete =ForeignKey.SET_DEFAULT,
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
    @ColumnInfo(name = "parent_category_id", defaultValue = "1") val categoryId: Int,
    val memo: String?=null,
)



@Entity(
    tableName = "category", indices = [Index(
        value = ["category_name", "parentId"], unique = true
    )]
)
data class Category(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "category_id") var id: Int = 0,

    @ColumnInfo(name = "category_name") var name: String,
    var parentId: Int? = null,

     var color: ColorData? = null,
    //var iconText: IconText?=null


) {
    /*fun getColor(): androidx.compose.ui.graphicsColor? {

        return color?.let { Color(it) }
    }*/

    fun addChildren(vararg child: Category): Array<Category> =
        if (this.id != 0 && parentId == null) {
            arrayOf(this, *child.map {
                it.parentId = this.id
                it
            }.toTypedArray())
        } else {
            throw IllegalArgumentException()
        }


    companion object {

        private val comparator = Comparator<Category> { o1, o2 ->
            if (o1.id == 0) {
                1
            } else if (o2.id == 0) {
                -1
            } else {
                o1.id.compareTo(o2.id)
            }
        }
        /**
         * 一気に追加するときにidが指定されているものが先にくるようにする
         *0は最後にー
         * */
        fun Array<out Category>.sortById(): Array<Category> = this.sortedWith(comparator).toTypedArray()

    }
}


data class CategoryAndProductItem(
    @Embedded val category: Category,
    @Relation(parentColumn = "category_id", entityColumn = "parent_category_id") val itemDatas: List<ItemData>
)


data class DateAndPriceProductItem(
    val date: Date,
    val price: Int,
)
@Dao
abstract class ItemDataDao {

    @Query("SELECT date FROM item_data  WHERE :minDate <= date AND :maxDate >= date")
     abstract fun getHasDataDaysFromDateRange(minDate: Date, maxDate: Date): Flow<List<Date>>

    @Query("SELECT * FROM item_data WHERE date = :date")
    abstract fun getItemsFromDate(date: Date): List<ItemData>


    @Insert
    abstract fun insertAll(vararg users: ItemData)

    @Delete
    abstract fun delete(user: ItemData)


    @Query("SELECT date,price from item_data  WHERE :minDate <= date AND :maxDate >= date")
    abstract fun getDateRangeSummary(minDate: Date, maxDate: Date):Flow<List<DateAndPriceProductItem>>


}

@Dao
interface CategoryDao {
    /**
     * よいこのみんなは[PrimaryKey.autoGenerate]による重複を防ぐため [sortById]を使ってから入れようね！！
     * */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg categories: Category)

    @Delete
    fun delete(category: Category)

    @Query("SELECT * FROM category")
    fun getAllCategory(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE category_id = :parentId")
    fun getCategoryFromParent(parentId: Int): List<Category>

    @Transaction
    @Query("SELECT * FROM category WHERE category_id IN (:categoryIds)")
    fun getCategoryByIds(vararg categoryIds: Int): List<CategoryAndProductItem>

    /*@Transaction
      @Query("SELECT * FROM Category")
      abstract fun getCategoryAndProductItemList(): Array<CategoryAndProductItem>*/

}


@Database(
    entities = [ItemData::class, Category::class],
    version = 1,
    exportSchema = true,
    // autoMigrations = [AutoMigration(from = 2, to = 3)]
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

                        ioThread{

                            getInstance(context).categoryDao().insertAll(*DEFAULT_CATEGORIES)
                        }
                    }
                }).build()


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
    }
}

data class ColorData (@ColorInt var colorInt: Int){
    fun toColor(): Color {
        return Color(colorInt)
    }
    companion object{
        fun Color.toColorData()=ColorData(this.toArgb())

    }
}



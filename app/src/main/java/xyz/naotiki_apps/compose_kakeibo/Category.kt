package xyz.naotiki_apps.compose_kakeibo

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById

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
    var iconText: IconText?=null


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
        fun Iterable<Category>.parseAllCategoriesWithChildren(): Map<Category, List<Category>?> {
            val parents = this.filter {
                it.parentId == null
            }
            val children = this.filter {
                it.parentId != null
            }
            return parents.associateWith {
                children.filter { child ->
                    child.parentId == it.id
                }.ifEmpty { null }
            }
        }

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

@Dao
interface CategoryDao {
    /**
     * よいこのみんなは[PrimaryKey.autoGenerate]による重複を防ぐため [Category.sortById]を使ってから入れようね！！
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

data class ColorData(@ColorInt var colorInt: Int) {
    fun toColor(): Color {
        return Color(colorInt)
    }
    companion object {
        fun Color.toColorData() = ColorData(this.toArgb())
    }
}

//一文字だけ
data class IconText(var iconText: String) {
    init {
        iconText = iconText.firstLetter()
    }
}

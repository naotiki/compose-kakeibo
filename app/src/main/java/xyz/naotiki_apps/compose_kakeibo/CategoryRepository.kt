package xyz.naotiki_apps.compose_kakeibo

import xyz.naotiki_apps.compose_kakeibo.Category.Companion.sortById

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAllCategories() = categoryDao.getAllCategory()

    fun addCategories(vararg category: Category) {
        ioThread {
            categoryDao.insertAll(*category.sortById())
        }
    }
}

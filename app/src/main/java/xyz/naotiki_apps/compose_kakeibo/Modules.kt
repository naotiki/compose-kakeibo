package xyz.naotiki_apps.compose_kakeibo

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//DIて便利
@Module
@InstallIn(SingletonComponent::class)
object Modules {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = AppDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideCategoryDao(db: AppDatabase) = db.categoryDao()

    @Singleton
    @Provides
    fun provideItemDataDao(db: AppDatabase) = db.itemDataDao()

    @Singleton
    @Provides
    fun provideCategoryRepository(categoryDao: CategoryDao) = CategoryRepository(categoryDao)

    @Singleton
    @Provides
    fun provideItemDataRepository(itemDataDao: ItemDataDao) = ItemDataRepository(itemDataDao)

}
